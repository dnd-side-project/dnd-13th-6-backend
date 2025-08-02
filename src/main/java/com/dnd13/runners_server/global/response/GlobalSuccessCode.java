package com.dnd13.runners_server.global.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GlobalSuccessCode implements SuccessCode{
	SUCCESS(HttpStatus.OK, "G000", "요청에 성공했습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
