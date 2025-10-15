package com.runky.auth.domain.token;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.auth.domain.token.jwt.JwtTokenProvider;
import com.runky.auth.domain.token.jwt.component.JwtTokenGenerator;
import com.runky.auth.domain.token.jwt.component.JwtTokenStore;
import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class JwtTokenProviderTest {

	@Autowired
	JwtTokenProvider provider;

	@Autowired
	JwtTokenStore tokenStore;

	@Autowired
	JwtTokenGenerator tokenGenerator; // 저장 안된 RT 생성용

	@Autowired(required = true)
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		if (databaseCleanUp != null)
			databaseCleanUp.truncateAllTables();
	}

	@Test
	@DisplayName("createTokenPair(): AT/RT 발급 및 RT 저장")
	void create_success() {
		// given
		Long memberId = 1L;
		String role = "USER";

		// when
		JwtTokenPair pair = provider.createTokenPair(memberId, role);

		// then
		assertThat(pair.accessToken()).isNotBlank();
		assertThat(pair.refreshToken()).isNotBlank();
		assertThat(tokenStore.existsRefreshToken(memberId, pair.refreshToken())).isTrue();
	}

	@Test
	@DisplayName("rotateTokens(): 기존 RT를 소비(무효화)하고 새 AT/RT 발급")
	void rotate_success() {
		// given
		Long memberId = 10L;
		String role = "ADMIN";
		JwtTokenPair first = provider.createTokenPair(memberId, role);
		assertThat(tokenStore.existsRefreshToken(memberId, first.refreshToken())).isTrue();

		// when
		JwtTokenPair rotated = provider.rotateTokens(first.refreshToken());

		// then
		assertThat(rotated.refreshToken()).isNotEqualTo(first.refreshToken());
		assertThat(tokenStore.existsRefreshToken(memberId, first.refreshToken())).isFalse();
		assertThat(tokenStore.existsRefreshToken(memberId, rotated.refreshToken())).isTrue();
	}

	@Test
	@DisplayName("rotateTokens(): 저장되지 않은 RT로 회전 시 InvalidTokenException")
	void rotate_invalid_token_throws() {
		// given: 저장되지 않은 RT 생성
		Long ghostMemberId = 77L;
		String role = "USER";
		String notSavedRt = tokenGenerator.generateTokenPair(ghostMemberId, role).refreshToken();

		// 방어적 확인
		assertThat(tokenStore.existsRefreshToken(ghostMemberId, notSavedRt)).isFalse();

		// expect
		assertThatThrownBy(() -> provider.rotateTokens(notSavedRt))
			.isInstanceOf(InvalidTokenException.class);
	}
}
