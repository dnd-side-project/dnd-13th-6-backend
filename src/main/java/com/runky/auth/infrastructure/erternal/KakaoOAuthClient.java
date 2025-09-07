package com.runky.auth.infrastructure.erternal;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.runky.auth.application.port.OAuthClient;
import com.runky.auth.config.props.KakaoProperties;
import com.runky.auth.domain.vo.OAuthUserInfo;
import com.runky.auth.infrastructure.erternal.dto.KakaoTokenResponse;
import com.runky.auth.infrastructure.erternal.dto.KakaoUserInfoResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

	private final KakaoApiHttpClient kakaoApiHttpClient;
	private final KakaoProperties props;

	@Override
	public String fetchAccessToken(String authorizationCode) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", props.clientId());
		body.add("redirect_uri", props.redirectUrl());
		body.add("code", authorizationCode);

		KakaoTokenResponse tokenResponse = kakaoApiHttpClient.getAccessToken(body);

		return tokenResponse.accessToken();
	}

	@Override
	public OAuthUserInfo fetchUserInfo(String accessToken) {
		String authorizationHeader = "Bearer " + accessToken;

		KakaoUserInfoResponse userInfoResponse = kakaoApiHttpClient.getUserInfo(authorizationHeader);

		return new OAuthUserInfo(
			"kakao",
			String.valueOf(userInfoResponse.id())
		);
	}

	public String devFetchAccessToken(String authorizationCode) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", props.clientId());
		body.add("redirect_uri", "https://api.runky.store/dev/api/auth/login/oauth2/code/kakao");
		body.add("code", authorizationCode);

		KakaoTokenResponse tokenResponse = kakaoApiHttpClient.getAccessToken(body);

		return tokenResponse.accessToken();
	}
}
