package com.runky.auth.domain;

public interface OAuthClient {
	String fetchAccessToken(String authorizationCode);

	OAuthUserInfo fetchUserInfo(String accessToken);

	String fetchAccessTokenForBranch(String authorizationCode, String branch);
}
