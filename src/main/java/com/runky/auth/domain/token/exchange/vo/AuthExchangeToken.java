package com.runky.auth.domain.token.exchange.vo;

import java.time.Instant;

import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;

import lombok.Builder;
import lombok.Getter;

/**
 * 인증 교환 토큰 (도메인 객체)
 */
@Getter
public class AuthExchangeToken {

	private final String id;
	private final Instant expiresAt;

	@Builder
	private AuthExchangeToken(String id, Instant expiresAt) {
		if (id == null || id.isBlank()) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}
		if (expiresAt == null) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		this.id = id;
		this.expiresAt = expiresAt;
	}

	/**
	 * 인증 교환 토큰 발급
	 */
	public static AuthExchangeToken issue(String id, Instant expiresAt) {
		return AuthExchangeToken.builder()
			.id(id)
			.expiresAt(expiresAt)
			.build();
	}

	/**
	 * 토큰 ID 반환
	 */
	public String idValue() {
		return id;
	}
}
