package com.runky.auth.api;

public final class AuthRequest {
	private AuthRequest() {
	}

	public record Signup(String nickname) {
	}
}
