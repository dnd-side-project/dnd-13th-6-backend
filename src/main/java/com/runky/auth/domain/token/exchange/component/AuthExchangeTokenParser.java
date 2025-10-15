package com.runky.auth.domain.token.exchange.component;

import org.springframework.stereotype.Component;

import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthExchangeTokenParser {

	private final AuthExchangeTokenStore tokenStore;

	public boolean validate(String token) {
		return tokenStore.exists(token);
	}

	public Long parse(String token) {
		return tokenStore.retrieve(token)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.INVALID_TOKEN));
	}
}
