package com.runky.auth.domain;

public record OAuthUserInfo(
	String provider,
	String providerId
) {
}
