package com.runky.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {

	@InjectMocks
	AuthFacade facade;

	@Mock
	AuthService authService;
	@Mock
	MemberReader memberReader;
	@Mock
	MemberRegistrar memberRegistrar;
	@Mock
	MemberService memberService;
	@Mock
	ApplicationEventPublisher eventPublisher;

	// ============= completeSignup =============

	@Test
	@DisplayName("completeSignup(): 정상 플로우 — 토큰 소비 → 회원 등록 → 교환 토큰 발급 → 이벤트 발행")
	void completeSignup_success() {
		// given
		String signupToken = "signup-token";
		OAuthUserInfo oauth = new OAuthUserInfo("KAKAO", "kakao-123");
		when(authService.consumeSignupToken(signupToken)).thenReturn(oauth);
		when(memberReader.existsByExternalAccount("KAKAO", "kakao-123")).thenReturn(false);

		MemberInfo.Summary newMember = mock(MemberInfo.Summary.class);
		when(newMember.id()).thenReturn(100L);
		when(memberRegistrar.registerFromExternal(any(MemberCommand.RegisterFromExternal.class)))
			.thenReturn(newMember);

		when(authService.issueAuthExchangeToken(100L)).thenReturn("auth-exchange-token");

		AuthCriteria.AdditionalSignUpData data = new AuthCriteria.AdditionalSignUpData("닉네임");

		// when
		AuthResult.SignupResponseAction action = facade.completeSignup(signupToken, data);

		// then
		assertThat(action).isInstanceOf(AuthResult.SignupResponseAction.SignupCompleteResponse.class);
		AuthResult.SignupResponseAction.SignupCompleteResponse res = (AuthResult.SignupResponseAction.SignupCompleteResponse)action;
		assertThat(res.authExchangeToken()).isEqualTo("auth-exchange-token");

	}

	@Test
	@DisplayName("completeSignup(): 이미 가입된 외부 계정이면 DuplicateMemberException")
	void completeSignup_duplicate_throws() {
		// given
		String signupToken = "signup-token";
		OAuthUserInfo oauth = new OAuthUserInfo("KAKAO", "kakao-123");
		when(authService.consumeSignupToken(signupToken)).thenReturn(oauth);
		when(memberReader.existsByExternalAccount("KAKAO", "kakao-123")).thenReturn(true);

		AuthCriteria.AdditionalSignUpData data = new AuthCriteria.AdditionalSignUpData("닉네임");

		// expect
		assertThatThrownBy(() -> facade.completeSignup(signupToken, data))
			.isInstanceOf(DuplicateMemberException.class);

	}

	// ============= exchangeAuthToken =============

	@Test
	@DisplayName("exchangeAuthToken(): 교환 토큰 소비 후 멤버 조회 → JWT 발급 반환")
	void exchangeAuthToken_success() {
		// given
		String exchangeToken = "ex-token";
		when(authService.consumeAuthExchangeToken(exchangeToken)).thenReturn(200L);

		Member member = mock(Member.class, RETURNS_DEEP_STUBS);
		when(memberService.getMember(new MemberCommand.Find(200L))).thenReturn(member);
		when(member.getRole().name()).thenReturn("USER");

		AuthInfo.TokenPair pair = mock(AuthInfo.TokenPair.class);
		when(pair.accessToken()).thenReturn("AT");
		when(pair.refreshToken()).thenReturn("RT");
		when(authService.issueTokens(200L, "USER")).thenReturn(pair);

		// when
		AuthInfo.TokenPair result = facade.exchangeAuthToken(exchangeToken);

		// then
		assertThat(result).isNotNull();
		assertThat(result.accessToken()).isEqualTo("AT");
		assertThat(result.refreshToken()).isEqualTo("RT");

	}

	// ============= refreshTokens =============

	@Test
	@DisplayName("refreshTokens(): RT 회전 위임 결과 반환")
	void refreshTokens_success() {
		// given
		String rt = "refresh.token";
		AuthInfo.TokenPair rotated = mock(AuthInfo.TokenPair.class);
		when(rotated.accessToken()).thenReturn("newAT");
		when(rotated.refreshToken()).thenReturn("newRT");
		when(authService.rotateTokens(rt)).thenReturn(rotated);

		// when
		AuthInfo.TokenPair result = facade.refreshTokens(rt);

		// then
		assertThat(result.accessToken()).isEqualTo("newAT");
		assertThat(result.refreshToken()).isEqualTo("newRT");
	}

	// ============= logout =============

	@Test
	@DisplayName("logout(): RT 기반 revoke 위임 — 예외 없이 수행")
	void logout_success() {
		// given
		String rt = "refresh.token";

		// when / then
		assertThatCode(() -> facade.logout(rt)).doesNotThrowAnyException();
	}
}
