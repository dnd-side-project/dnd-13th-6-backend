package com.runky.running.infra.websocket.auth;

import static com.runky.running.constants.RunningSocketConstants.*;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;

@Component
public class StompAuthorizationHeaderTokenResolver {

	public String resolve(StompHeaderAccessor accessor) {
		String rawHeader = accessor.getFirstNativeHeader(AUTHORIZATION);

		if (!StringUtils.hasText(rawHeader)) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		if (!rawHeader.toLowerCase().startsWith(BEARER_.toLowerCase())) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		String token = rawHeader.substring(BEARER_.length()).trim();

		if (!StringUtils.hasText(token)) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		return token;
	}
}

