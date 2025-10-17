package com.runky.auth.application;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;

public final class AuthCriteria {

	public record AdditionalSignUpData(String nickname) {
		private static final int MAX_NICKNAME_LENGTH = 10;
		private static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]+$";

		public AdditionalSignUpData {
			// Compact constructor - 파라미터 이름이 자동으로 nickname이 됩니다
			if (nickname == null || nickname.isBlank()) {
				throw new GlobalException(MemberErrorCode.BLANK_NICKNAME);
			}
			if (nickname.length() > MAX_NICKNAME_LENGTH) {
				throw new GlobalException(MemberErrorCode.OVER_LENGTH_NICKNAME);
			}
			if (!nickname.matches(NICKNAME_PATTERN)) {
				throw new GlobalException(MemberErrorCode.INVALID_FORMAT_NICKNAME);
			}
		}
	}
}
