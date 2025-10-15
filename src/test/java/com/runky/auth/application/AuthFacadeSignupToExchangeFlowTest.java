package com.runky.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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

@ExtendWith(MockitoExtension.class)
class AuthFacadeSignupToExchangeFlowTest {

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

	@Test
	@DisplayName("신규 회원가입 완료 후, 교환 토큰으로 JWT 교환까지 성공한다")
	void completeSignup_then_exchangeAuthToken_success() {
		// ----- 준비: 회원가입 완료 흐름 -----
		String signupToken = "signup-token";
		OAuthUserInfo oauth = new OAuthUserInfo("KAKAO", "kakao-123");
		when(authService.consumeSignupToken(signupToken)).thenReturn(oauth);
		when(memberReader.existsByExternalAccount("KAKAO", "kakao-123")).thenReturn(false);

		MemberInfo.Summary newMember = mock(MemberInfo.Summary.class);
		when(newMember.id()).thenReturn(100L);
		when(memberRegistrar.registerFromExternal(any(MemberCommand.RegisterFromExternal.class)))
			.thenReturn(newMember);

		String exchangeToken = "ex-token";
		when(authService.issueAuthExchangeToken(100L)).thenReturn(exchangeToken);

		// ----- 실행: 회원가입 완료 -----
		AuthCriteria.AdditionalSignUpData data = new AuthCriteria.AdditionalSignUpData("one-dish");
		AuthResult.SignupResponseAction signupRes = facade.completeSignup(signupToken, data);

		assertThat(signupRes).isInstanceOf(AuthResult.SignupResponseAction.SignupCompleteResponse.class);
		AuthResult.SignupResponseAction.SignupCompleteResponse signupOk =
			(AuthResult.SignupResponseAction.SignupCompleteResponse)signupRes;
		assertThat(signupOk.authExchangeToken()).isEqualTo(exchangeToken);

		// 상호작용 순서 검증(선택)
		InOrder signupOrder = inOrder(authService, memberReader, memberRegistrar, eventPublisher);
		signupOrder.verify(authService).consumeSignupToken(signupToken);
		signupOrder.verify(memberReader).existsByExternalAccount("KAKAO", "kakao-123");
		signupOrder.verify(memberRegistrar).registerFromExternal(any(MemberCommand.RegisterFromExternal.class));
		signupOrder.verify(authService).issueAuthExchangeToken(100L);
		signupOrder.verify(eventPublisher).publishEvent(any(AuthEvent.SignupCompleted.class));

		// ----- 준비: 교환 토큰 → JWT 교환 흐름 -----
		when(authService.consumeAuthExchangeToken(exchangeToken)).thenReturn(100L);

		Member member = mock(Member.class, RETURNS_DEEP_STUBS);
		when(memberService.getMember(new MemberCommand.Find(100L))).thenReturn(member);
		when(member.getRole().name()).thenReturn("USER");

		AuthInfo.TokenPair issued = mock(AuthInfo.TokenPair.class);
		when(issued.accessToken()).thenReturn("AT");
		when(issued.refreshToken()).thenReturn("RT");
		when(authService.issueTokens(100L, "USER")).thenReturn(issued);

		// ----- 실행: 교환 토큰으로 JWT 교환 -----
		AuthInfo.TokenPair exchanged = facade.exchangeAuthToken(exchangeToken);

		// ----- 검증: JWT 획득 -----
		assertThat(exchanged).isNotNull();
		assertThat(exchanged.accessToken()).isEqualTo("AT");
		assertThat(exchanged.refreshToken()).isEqualTo("RT");

		// 교환 플로우 상호작용 순서 검증(선택)
		InOrder exchangeOrder = inOrder(authService, memberService);
		exchangeOrder.verify(authService).consumeAuthExchangeToken(exchangeToken);
		exchangeOrder.verify(memberService).getMember(new MemberCommand.Find(100L));
		exchangeOrder.verify(authService).issueTokens(100L, "USER");
	}
}
