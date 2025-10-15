package com.runky.auth.domain.token.exchange.component;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.token.exchange.vo.AuthExchangeToken;

/**
 * AuthExchangeToken Factory
 * - Secure random 토큰 ID 생성
 * - 만료시간 계산 (30초)
 */
@Component
public class AuthExchangeTokenFactory {

	private static final long EXPIRATION_SECONDS = 30;
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * AuthExchangeToken 생성 (만료시간 포함)
	 */
	public AuthExchangeToken create() {
		String tokenId = generateTokenId();
		Instant expiresAt = Instant.now().plus(EXPIRATION_SECONDS, ChronoUnit.SECONDS);

		return AuthExchangeToken.issue(tokenId, expiresAt);
	}

	/**
	 * Cryptographically secure random 토큰 ID 생성
	 */
	private String generateTokenId() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
