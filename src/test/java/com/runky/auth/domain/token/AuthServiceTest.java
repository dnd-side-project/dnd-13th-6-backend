package com.runky.auth.domain.token;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.auth.domain.AuthInfo;
import com.runky.auth.domain.AuthService;
import com.runky.auth.domain.OAuthUserInfo;
import com.runky.auth.domain.token.jwt.component.JwtTokenStore;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class AuthServiceTest {

	@Autowired
	AuthService authService;

	@Autowired
	JwtTokenStore tokenStore;

	@Autowired(required = false)
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		if (databaseCleanUp != null)
			databaseCleanUp.truncateAllTables();
	}

	// =========================
	// JWT 토큰 관리
	// =========================

	@Test
	@DisplayName("issueTokens(): AT/RT를 발급하고 RT는 저장된다")
	void issueTokens_success() {
		// given
		Long memberId = 1L;
		String role = "USER";

		// when
		AuthInfo.TokenPair pair = authService.issueTokens(memberId, role);

		// then
		assertThat(pair.accessToken()).isNotBlank();
		assertThat(pair.refreshToken()).isNotBlank();
		assertThat(tokenStore.existsRefreshToken(memberId, pair.refreshToken())).isTrue();
	}

	@Test
	@DisplayName("rotateTokens(): 제시된 RT를 회전하여 새 AT/RT를 발급하고, 이전 RT는 무효화된다")
	void rotateTokens_success() {
		// given
		Long memberId = 10L;
		String role = "ADMIN";
		AuthInfo.TokenPair first = authService.issueTokens(memberId, role);
		assertThat(tokenStore.existsRefreshToken(memberId, first.refreshToken())).isTrue();

		// when
		AuthInfo.TokenPair rotated = authService.rotateTokens(first.refreshToken());

		// then
		assertThat(rotated.refreshToken()).isNotBlank();
		assertThat(rotated.refreshToken()).isNotEqualTo(first.refreshToken());
		assertThat(tokenStore.existsRefreshToken(memberId, first.refreshToken())).isFalse();
		assertThat(tokenStore.existsRefreshToken(memberId, rotated.refreshToken())).isTrue();
	}

	@Test
	@DisplayName("rotateTokens(): 저장되지 않은 RT로 회전 시 InvalidTokenException")
	void rotateTokens_invalid_throws() {
		// given: 존재하지 않는(저장 안 된) RT
		String notSavedRt = "this.is.not.saved.refresh.token";

		// expect
		assertThatThrownBy(() -> authService.rotateTokens(notSavedRt))
			.isInstanceOf(InvalidTokenException.class);
	}

	@Test
	@DisplayName("revokeTokens(): RT를 가진 멤버의 RT를 모두 폐기한다(입력 RT 기반으로 memberId 추출)")
	void revokeTokens_success() {
		// given
		Long memberId = 22L;
		String role = "USER";
		AuthInfo.TokenPair pair = authService.issueTokens(memberId, role);
		assertThat(tokenStore.existsRefreshToken(memberId, pair.refreshToken())).isTrue();

		// when
		authService.revokeTokens(pair.refreshToken());

		// then
		assertThat(tokenStore.existsRefreshToken(memberId, pair.refreshToken())).isFalse();
	}

	@Test
	@DisplayName("deleteRefreshToken(memberId): 해당 멤버의 모든 RT를 삭제한다")
	void deleteRefreshToken_success() {
		// given: 동일 멤버에 RT 2개 누적(다중 RT 허용 시나리오)
		Long memberId = 33L;
		String role = "USER";
		AuthInfo.TokenPair p1 = authService.issueTokens(memberId, role);
		AuthInfo.TokenPair p2 = authService.issueTokens(memberId, role);
		assertThat(tokenStore.existsRefreshToken(memberId, p1.refreshToken())).isTrue();
		assertThat(tokenStore.existsRefreshToken(memberId, p2.refreshToken())).isTrue();

		// when
		authService.deleteRefreshToken(memberId);

		// then
		assertThat(tokenStore.existsRefreshToken(memberId, p1.refreshToken())).isFalse();
		assertThat(tokenStore.existsRefreshToken(memberId, p2.refreshToken())).isFalse();
	}

	@Test
	@DisplayName("validateAccessToken(): 발급한 AT는 유효로 검증된다")
	void validateAccessToken_success() {
		// given
		Long memberId = 44L;
		String role = "USER";
		AuthInfo.TokenPair pair = authService.issueTokens(memberId, role);

		// when
		boolean valid = authService.validateAccessToken(pair.accessToken());

		// then
		assertThat(valid).isTrue();
	}

	@Test
	@DisplayName("extractMemberId(): AT로부터 memberId를 추출한다")
	void extractMemberId_success() {
		// given
		Long memberId = 55L;
		String role = "USER";
		AuthInfo.TokenPair pair = authService.issueTokens(memberId, role);

		// when
		Long extracted = authService.extractMemberId(pair.accessToken());

		// then
		assertThat(extracted).isEqualTo(memberId);
	}

	// =========================
	// SignupToken 관리
	// =========================

	@Test
	@DisplayName("issueSignupToken()/consumeSignupToken(): 일회용 회원가입 토큰 발급 및 소비")
	void signupToken_issue_and_consume_success() {
		// given (필요 시 OAuthUserInfo 필드 보완)
		OAuthUserInfo oauth = new OAuthUserInfo("KAKAO", "kakao-123");

		// when
		String signupToken = authService.issueSignupToken(oauth);

		// then
		assertThat(signupToken).isNotBlank();

		OAuthUserInfo consumed = authService.consumeSignupToken(signupToken);
		assertThat(consumed).isNotNull();
		assertThat(consumed.provider()).isEqualTo("KAKAO");
		assertThat(consumed.providerId()).isEqualTo("kakao-123");
	}

	@Test
	@DisplayName("consumeSignupToken(): 잘못된 토큰이면 InvalidTokenException")
	void signupToken_invalid_throws() {
		assertThatThrownBy(() -> authService.consumeSignupToken("bogus-signup-token"))
			.isInstanceOf(InvalidTokenException.class);
	}

	// =========================
	// AuthExchangeToken 관리
	// =========================

	@Test
	@DisplayName("issueAuthExchangeToken()/consumeAuthExchangeToken(): 일회용 교환 토큰 발급 및 소비")
	void authExchangeToken_issue_and_consume_success() {
		// given
		Long memberId = 66L;

		// when
		String exToken = authService.issueAuthExchangeToken(memberId);

		// then
		assertThat(exToken).isNotBlank();

		Long consumedMemberId = authService.consumeAuthExchangeToken(exToken);
		assertThat(consumedMemberId).isEqualTo(memberId);
	}

	@Test
	@DisplayName("consumeAuthExchangeToken(): 잘못된 토큰이면 InvalidTokenException")
	void authExchangeToken_invalid_throws() {
		assertThatThrownBy(() -> authService.consumeAuthExchangeToken("invalid-exchange-token"))
			.isInstanceOf(InvalidTokenException.class);
	}
}
