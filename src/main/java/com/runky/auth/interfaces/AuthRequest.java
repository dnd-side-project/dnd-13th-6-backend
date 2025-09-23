package com.runky.auth.interfaces;

public final class AuthRequest {
	private AuthRequest() {
	}

	public record Signup(String nickname) {
	}
}
