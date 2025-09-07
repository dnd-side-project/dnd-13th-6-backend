package com.runky.auth.application.port;

import com.runky.auth.domain.vo.OAuthUserInfo;

public interface OAuthClient {
	String fetchAccessToken(String authorizationCode);

	OAuthUserInfo fetchUserInfo(String accessToken);

	String devFetchAccessToken(String authorizationCode);
}
