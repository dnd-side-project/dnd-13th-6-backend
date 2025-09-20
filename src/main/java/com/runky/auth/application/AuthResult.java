package com.runky.auth.application;

public final class AuthResult {
	private AuthResult() {
	}

	public enum AuthStatus {NEW_USER, EXISTING_USER}

	public record SigninComplete(String accessToken, String refreshToken) {
	}

	public record OAuthLogin(boolean isNewUser, String signupToken, String accessToken, String refreshToken,
							 AuthStatus authStatus
	) {
		public static OAuthLogin newUser(String signupToken) {
			return new OAuthLogin(true, signupToken, null, null, AuthStatus.NEW_USER);
		}

		public static OAuthLogin existing(String accessToken, String refreshToken) {
			return new OAuthLogin(false, null, accessToken, refreshToken, AuthStatus.EXISTING_USER);
		}
	}

	public record rotatedToken(String accessToken, String refreshToken) {
	}

}
