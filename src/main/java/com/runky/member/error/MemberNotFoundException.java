package com.runky.member.error;

import com.runky.global.error.GlobalException;

public class MemberNotFoundException extends GlobalException {
	public MemberNotFoundException() {
		super(MemberErrorCode.MEMBER_NOT_FOUND);
	}
}
