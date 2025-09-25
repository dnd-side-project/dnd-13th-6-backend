package com.runky.auth.application;

public final class AuthEvent {
	private AuthEvent() {
	}

	public record SignupCompleted(Long memberId) {
	}
}
