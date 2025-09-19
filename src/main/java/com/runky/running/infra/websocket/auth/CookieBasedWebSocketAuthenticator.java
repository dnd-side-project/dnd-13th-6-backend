package com.runky.running.infra.websocket.auth;

import java.util.Arrays;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.runky.auth.domain.AuthService;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieBasedWebSocketAuthenticator {
	private final String ACCESS_TOKEN = "accessToken";

	private final AuthService authService;

	public Authentication authenticate(ServerHttpRequest request) {
		if (!(request instanceof ServletServerHttpRequest servletRequest))
			return null;

		Cookie[] cookies = servletRequest.getServletRequest().getCookies();
		if (cookies == null) {
			return null;
		}

		return Arrays.stream(cookies)
			.filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()))
			.map(Cookie::getValue)
			.filter(StringUtils::hasText)
			.findFirst()
			.map(authService::authenticate)
			.orElse(null);
	}
}
