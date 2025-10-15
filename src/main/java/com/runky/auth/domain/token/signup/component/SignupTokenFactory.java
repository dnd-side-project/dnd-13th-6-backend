package com.runky.auth.domain.token.signup.component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.token.signup.vo.SignupToken;

/**
 * SignupToken Factory
 * - 토큰 ID 생성
 * - 만료시간 계산
 */
@Component
public class SignupTokenFactory {

	private static final long EXPIRATION_MINUTES = 10;

	/**
	 * SignupToken 생성 (만료시간 포함)
	 */
	public SignupToken create() {
		String tokenId = generateTokenId();
		Instant expiresAt = Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);

		return SignupToken.issue(tokenId, expiresAt);
	}

	/**
	 * 토큰 ID 생성 (UUID, 하이픈 제거)
	 */
	private String generateTokenId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
