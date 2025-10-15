package com.runky.auth.domain;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.token.exchange.AuthExchangeTokenProvider;
import com.runky.auth.domain.token.jwt.JwtTokenProvider;
import com.runky.auth.domain.token.jwt.component.JwtTokenParser;
import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;
import com.runky.auth.domain.token.jwt.vo.TokenClaims;
import com.runky.auth.domain.token.refresh.RefreshTokenRepository;
import com.runky.auth.domain.token.signup.SignupTokenProvider;
import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;
import com.runky.global.security.auth.MemberPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final OAuthClient oAuthClient;
	private final SignupTokenProvider signupTokenProvider;
	private final AuthExchangeTokenProvider authExchangeTokenProvider;
	private final JwtTokenParser parser;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	// ========================================
	// OAuth 처리
	// ========================================

	@Transactional(readOnly = true)
	public OAuthUserInfo fetchOAuthUserInfo(String authorizationCode) {
		String accessToken = oAuthClient.fetchAccessToken(authorizationCode);
		OAuthUserInfo userInfo = oAuthClient.fetchUserInfo(accessToken);

		log.debug("OAuth user info fetched: provider={}, providerId={}",
			userInfo.provider(), userInfo.providerId());

		return userInfo;
	}

	// ========================================
	// JWT 토큰 관리
	// ========================================

	@Transactional
	public AuthInfo.TokenPair issueTokens(Long memberId, String role) {
		JwtTokenPair tokens = jwtTokenProvider.createTokenPair(memberId, role);

		log.info("JWT tokens issued: memberId={}", memberId);

		return AuthInfo.TokenPair.from(tokens);
	}

	@Transactional
	public AuthInfo.TokenPair rotateTokens(String refreshToken) {
		JwtTokenPair newTokens = jwtTokenProvider.rotateTokens(refreshToken);

		log.info("JWT tokens rotated");

		return AuthInfo.TokenPair.from(newTokens);
	}

	@Transactional
	public void revokeTokens(String refreshToken) {
		TokenClaims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
		jwtTokenProvider.revokeTokens(claims.memberId());

		log.info("JWT tokens revoked: memberId={}", claims.memberId());
	}

	@Transactional
	public void deleteRefreshToken(Long memberId) {
		refreshTokenRepository.deleteByMemberId(memberId);
	}

	public boolean validateAccessToken(String accessToken) {
		return jwtTokenProvider.validateAccessToken(accessToken);
	}

	public Long extractMemberId(String accessToken) {
		return jwtTokenProvider.getMemberIdFromAccessToken(accessToken);
	}

	// ========================================
	// SignupToken 관리
	// ========================================

	public String issueSignupToken(OAuthUserInfo oauthUserInfo) {
		String token = signupTokenProvider.createSignupToken(oauthUserInfo);

		log.info("SignupToken issued: provider={}, providerId={}",
			oauthUserInfo.provider(), oauthUserInfo.providerId());

		return token;
	}

	public OAuthUserInfo consumeSignupToken(String signupToken) {
		OAuthUserInfo userInfo = signupTokenProvider.consumeToken(signupToken);

		log.info("SignupToken consumed: provider={}, providerId={}",
			userInfo.provider(), userInfo.providerId());

		return userInfo;
	}

	// ========================================
	// AuthExchangeToken 관리
	// ========================================

	public String issueAuthExchangeToken(Long memberId) {
		String token = authExchangeTokenProvider.createExchangeToken(memberId);

		log.info("AuthExchangeToken issued: memberId={}", memberId);

		return token;
	}

	public Long consumeAuthExchangeToken(String authExchangeToken) {
		Long memberId = authExchangeTokenProvider.consumeToken(authExchangeToken);

		log.info("AuthExchangeToken consumed: memberId={}", memberId);

		return memberId;
	}

	public Authentication authenticate(String jwt) {

		TokenClaims claims = parser.parseAccessToken(jwt);
		MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());
		return new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
	}

	public MemberPrincipal principalOf(Authentication auth) {
		if (auth == null) {
			throw new GlobalException(AuthErrorCode.NULL_AUTHENTICATION);
		}

		Object principal = auth.getPrincipal();

		if (!(principal instanceof MemberPrincipal)) {
			throw new GlobalException(AuthErrorCode.UN_SUPPORT_AUTHENTICATION);
		}
		return (MemberPrincipal)principal;
	}

}
