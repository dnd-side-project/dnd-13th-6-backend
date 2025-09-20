package com.runky.auth.api;

public final class AuthResponse {

	private AuthResponse() {
	}

	public record NewUser(String nextAction) {
		public NewUser() {
			this("COMPLETE_SIGNUP");
		}
	}

	public record ExistingUser(String nextAction) {
		public ExistingUser() {
			this("LOGIN_DONE");
		}
	}
}
