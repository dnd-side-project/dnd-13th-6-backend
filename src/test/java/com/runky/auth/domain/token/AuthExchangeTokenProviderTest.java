package com.runky.auth.domain.token;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.auth.domain.token.exchange.AuthExchangeTokenProvider;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class AuthExchangeTokenProviderTest {

	@Autowired
	AuthExchangeTokenProvider provider;

	@Autowired(required = false)
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		if (databaseCleanUp != null)
			databaseCleanUp.truncateAllTables();
	}

	@Test
	@DisplayName("createExchangeToken(): 토큰 생성 후 validate/consume 성공")
	void create_and_consume_success() {
		// given
		Long memberId = 123L;

		// when
		String token = provider.createExchangeToken(memberId);

		// then
		assertThat(token).isNotBlank();
		assertThat(provider.validate(token)).isTrue();

		Long consumedMemberId = provider.consumeToken(token);
		assertThat(consumedMemberId).isEqualTo(memberId);
	}

	@Test
	@DisplayName("consumeToken(): 잘못된 토큰이면 InvalidTokenException")
	void consume_invalid_token_throws() {
		assertThatThrownBy(() -> provider.consumeToken("invalid.token.value"))
			.isInstanceOf(InvalidTokenException.class);
	}
}
