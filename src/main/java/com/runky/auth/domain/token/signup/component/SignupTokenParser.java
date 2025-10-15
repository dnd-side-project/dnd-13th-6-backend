package com.runky.auth.domain.token.signup.component;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.OAuthUserInfo;
import com.runky.auth.exception.domain.InvalidTokenException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignupTokenParser {

	private final SignupTokenStore tokenStore;

	public boolean validate(String token) {
		return tokenStore.exists(token);
	}

	public OAuthUserInfo parse(String token) {
		return tokenStore.retrieve(token)
			.orElseThrow(() -> new InvalidTokenException());
	}
}
