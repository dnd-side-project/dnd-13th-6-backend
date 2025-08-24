package com.runky.member.error;

import com.runky.global.error.GlobalException;

public class InvalidExternalAccountException extends GlobalException {
	public InvalidExternalAccountException() {
		super(MemberErrorCode.INVALID_EXTERNAL_ACCOUNT);
	}
}
