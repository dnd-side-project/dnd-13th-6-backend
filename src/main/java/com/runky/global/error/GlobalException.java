package com.runky.global.error;

import lombok.Getter;

/** 프로젝트 에러를 반환 할 때 사용하는 기본 클래스 errorCode를 주입받아야 한다. */
public class GlobalException extends RuntimeException {
	@Getter
	private final ErrorCode errorCode;

	/**
	 * Constructs a new GlobalException with the specified ErrorCode.
	 *
	 * The exception message is set to the message provided by the given ErrorCode.
	 *
	 * @param errorCode the ErrorCode representing the specific error condition
	 */
	public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
