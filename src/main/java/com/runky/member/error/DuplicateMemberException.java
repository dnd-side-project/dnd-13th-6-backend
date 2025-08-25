package com.runky.member.error;

import com.runky.global.error.GlobalException;

public class DuplicateMemberException extends GlobalException {
	public DuplicateMemberException() {
		super(MemberErrorCode.DUPLICATE_MEMBER);
	}
}
