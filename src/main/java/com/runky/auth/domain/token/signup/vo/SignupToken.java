package com.runky.auth.domain.token.signup.vo;

import java.time.Instant;

import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 토큰 (도메인 객체)
 */
@Getter
public class SignupToken {

	private final String id;
	private final Instant expiresAt;

	@Builder
	private SignupToken(String id, Instant expiresAt) {
		if (id == null || id.isBlank()) {
			throw new GlobalException(AuthErrorCode.SIGNUP_TOKEN_ID_BLANK);
		}
		if (expiresAt == null) {
			throw new GlobalException(AuthErrorCode.SIGNUP_TOKEN_TTL_BLANK);
		}

		this.id = id;
		this.expiresAt = expiresAt;
	}

	/**
	 * 회원가입 토큰 발급
	 */
	public static SignupToken issue(String id, Instant expiresAt) {
		return SignupToken.builder()
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
