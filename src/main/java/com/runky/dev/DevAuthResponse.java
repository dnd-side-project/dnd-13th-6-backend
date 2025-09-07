package com.runky.dev;

public sealed interface DevAuthResponse
	permits DevAuthResponse.NewUser, DevAuthResponse.ExistingUser, DevAuthResponse.Tokens {

	// 신규 유저: 회원가입 완료를 유도 (토큰은 아직 없음)
	record NewUser(String nextAction, String signupToken) implements DevAuthResponse {
		public NewUser(String signupToken) {
			this("COMPLETE_SIGNUP", signupToken);
		}
	}

	// 기존 유저: 로그인 직후 AT/RT를 JSON으로 반환
	record ExistingUser(String accessToken, String refreshToken) implements DevAuthResponse {
	}

	// 공통 토큰 페이로드(재발급 등)
	record Tokens(String accessToken, String refreshToken) implements DevAuthResponse {
	}
}
