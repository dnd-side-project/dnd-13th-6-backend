package com.runky.auth.domain;

public final class AuthCommand {
	private AuthCommand() {
	}

	public record OauthUserInfo(String provider, String providerId) {
	}

	public record AdditionalSignUpData(String nickname) {
	}
}
