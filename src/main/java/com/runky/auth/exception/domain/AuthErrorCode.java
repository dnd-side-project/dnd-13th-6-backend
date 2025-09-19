package com.runky.auth.exception.domain;

import org.springframework.http.HttpStatus;

import com.runky.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A101", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A102", "만료된 토큰입니다."),
	TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A103", "토큰 정보가 일치하지 않습니다."),
	TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "A104", "토큰이 필요합니다."),
	NULL_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "A105", "인증이 필요합니다"),
	SIGNUP_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A201", "회원가입 토큰이 만료되었습니다. 다시 로그인해주세요."),
	SIGNUP_TOKEN_ID_BLANK(HttpStatus.INTERNAL_SERVER_ERROR, "A202", "회원가입 토큰의 ID가 비어있습니다."),
	SIGNUP_TOKEN_TTL_BLANK(HttpStatus.INTERNAL_SERVER_ERROR, "A203", "회원가입 토큰의 ttl이 비어있습니다."),

	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
