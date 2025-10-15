package com.runky.auth.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.AuthInfo;
import com.runky.auth.domain.AuthService;
import com.runky.auth.domain.OAuthUserInfo;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.MemberService;
import com.runky.member.domain.dto.MemberInfo;
import com.runky.member.domain.service.MemberReader;
import com.runky.member.domain.service.MemberRegistrar;
import com.runky.member.error.DuplicateMemberException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFacade {

	private final AuthService authService;

	private final MemberReader memberReader;
	private final MemberRegistrar memberRegistrar;
	private final MemberService memberService;

	private final ApplicationEventPublisher eventPublisher;

	/**
	 * OAuth 로그인 처리
	 *
	 * 흐름:
	 * 1. OAuth 사용자 정보 조회
	 * 2. 회원 존재 여부 확인
	 * 3-1. 신규 → SignupToken 발급
	 * 3-2. 기존 → JWT 발급 + AuthExchangeToken 발급
	 */
	@Transactional
	public AuthResult.OAuthResponseAction handleOAuthLogin(String authorizationCode) {
		OAuthUserInfo oauthUserInfo = authService.fetchOAuthUserInfo(authorizationCode);

		boolean exists = memberReader.existsByExternalAccount(
			oauthUserInfo.provider(),
			oauthUserInfo.providerId()
		);

		if (!exists) {
			String signupToken = authService.issueSignupToken(oauthUserInfo);
			log.info("New user login: provider={}, providerId={}", oauthUserInfo.provider(),
				oauthUserInfo.providerId());

			return new AuthResult.OAuthResponseAction.NewUserRedirect(signupToken);
		} else {
			MemberInfo.Summary member = memberReader.getInfoByExternalAccount(
				oauthUserInfo.provider(),
				oauthUserInfo.providerId()
			);

			authService.issueTokens(member.id(), member.role().name());
			String authExchangeToken = authService.issueAuthExchangeToken(member.id());
			log.info("Existing user login: memberId={}", member.id());

			return new AuthResult.OAuthResponseAction.ExistingUserRedirect(authExchangeToken);
		}
	}

	@Transactional
	public AuthResult.OAuthResponseAction devHandleOAuthLogin(String authorizationCode) {
		OAuthUserInfo oauthUserInfo = authService.devFetchOAuthUserInfo(authorizationCode);

		boolean exists = memberReader.existsByExternalAccount(
			oauthUserInfo.provider(),
			oauthUserInfo.providerId()
		);

		if (!exists) {
			String signupToken = authService.issueSignupToken(oauthUserInfo);
			log.info("New user login: provider={}, providerId={}", oauthUserInfo.provider(),
				oauthUserInfo.providerId());

			return new AuthResult.OAuthResponseAction.NewUserRedirect(signupToken);
		} else {
			MemberInfo.Summary member = memberReader.getInfoByExternalAccount(
				oauthUserInfo.provider(),
				oauthUserInfo.providerId()
			);

			authService.issueTokens(member.id(), member.role().name());
			String authExchangeToken = authService.issueAuthExchangeToken(member.id());
			log.info("Existing user login: memberId={}", member.id());

			return new AuthResult.OAuthResponseAction.ExistingUserRedirect(authExchangeToken);
		}
	}

	/**
	 * 회원가입 완료
	 *
	 * 흐름:
	 * 1. SignupToken 소비
	 * 2. 중복 가입 방지
	 * 3. 회원 등록
	 * 4. JWT 발급
	 * 5. AuthExchangeToken 발급
	 * 6. 이벤트 발행
	 */
	@Transactional
	public AuthResult.SignupResponseAction completeSignup(String signupToken,
		AuthCriteria.AdditionalSignUpData additionalData) {
		OAuthUserInfo oauthUserInfo = authService.consumeSignupToken(signupToken);

		if (memberReader.existsByExternalAccount(oauthUserInfo.provider(), oauthUserInfo.providerId())) {
			throw new DuplicateMemberException();
		}

		MemberInfo.Summary newMember = memberRegistrar.registerFromExternal(
			new MemberCommand.RegisterFromExternal(
				oauthUserInfo.provider(),
				oauthUserInfo.providerId(),
				additionalData.nickname()
			)
		);

		String authExchangeToken = authService.issueAuthExchangeToken(newMember.id());

		eventPublisher.publishEvent(new AuthEvent.SignupCompleted(newMember.id()));
		log.info("Signup completed: memberId={}", newMember.id());

		return new AuthResult.SignupResponseAction.SignupCompleteResponse(authExchangeToken);
	}

	/**
	 * AuthExchangeToken → JWT 교환
	 *
	 * Authorization Code 패턴
	 */
	@Transactional(readOnly = true)
	public AuthInfo.TokenPair exchangeAuthToken(String authExchangeToken) {
		// 1. AuthExchangeToken 소비 (일회용)
		Long memberId = authService.consumeAuthExchangeToken(authExchangeToken);
		Member member = memberService.getMember(new MemberCommand.Find(memberId));

		// 3. JWT 재발급 (또는 캐시된 JWT 반환)
		AuthInfo.TokenPair tokens = authService.issueTokens(memberId, member.getRole().name());

		log.info("JWT tokens exchanged: memberId={}", memberId);

		return tokens;
	}

	/**
	 * JWT 갱신 (Refresh Token Rotation)
	 */
	@Transactional
	public AuthInfo.TokenPair refreshTokens(String refreshToken) {
		return authService.rotateTokens(refreshToken);
	}

	/**
	 * 로그아웃
	 */
	@Transactional
	public void logout(String refreshToken) {
		authService.revokeTokens(refreshToken);
	}
}
