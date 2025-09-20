package com.runky.auth.domain;

public final class AuthInfo {
	private AuthInfo() {
	}

	public record TokenPair(String accessToken, String refreshToken) {
	}

}
