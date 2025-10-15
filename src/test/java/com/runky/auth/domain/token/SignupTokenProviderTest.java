package com.runky.auth.domain.token;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.auth.domain.OAuthUserInfo;
import com.runky.auth.domain.token.signup.SignupTokenProvider;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class SignupTokenProviderTest {

	@Autowired
	SignupTokenProvider provider;

	@Autowired(required = false)
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		if (databaseCleanUp != null)
			databaseCleanUp.truncateAllTables();
	}

	@Test
	@DisplayName("createSignupToken(): 토큰 생성 후 validate/consume 성공")
	void create_and_consume_success() {
		// given (필요 필드를 프로젝트 정의에 맞게 보완)
		OAuthUserInfo oauth = new OAuthUserInfo("KAKAO", "kakao-12345");

		// when
		String token = provider.createSignupToken(oauth);

		// then
		assertThat(token).isNotBlank();
		assertThat(provider.validate(token)).isTrue();

		OAuthUserInfo consumed = provider.consumeToken(token);
		assertThat(consumed).isNotNull();
		assertThat(consumed.provider()).isEqualTo("KAKAO");
		assertThat(consumed.providerId()).isEqualTo("kakao-12345");
	}

	@Test
	@DisplayName("consumeToken(): 잘못된 토큰이면 InvalidTokenException")
	void consume_invalid_token_throws() {
		assertThatThrownBy(() -> provider.consumeToken("bogus.signup.token"))
			.isInstanceOf(InvalidTokenException.class);
	}
}
