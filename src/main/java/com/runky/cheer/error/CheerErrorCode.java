package com.runky.cheer.error;

import org.springframework.http.HttpStatus;

import com.runky.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheerErrorCode implements ErrorCode {

	ALREADY_DO_CHEER(HttpStatus.BAD_REQUEST, "C101", "이미 런닝 응원 메세지를 보냈습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
