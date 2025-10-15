package com.runky.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.runky.auth.domain.token.jwt.JwtTokenProvider;

@Component
public class TestTokenIssuer {

	private final JwtTokenProvider tokenProvider;

	public TestTokenIssuer(final JwtTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	public HttpHeaders issue(long memberId, String role) {
		var issued = tokenProvider.createTokenPair(memberId, role);
		String accessToken = issued.accessToken();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.COOKIE, "accessToken=" + accessToken);
		return headers;
	}
}
