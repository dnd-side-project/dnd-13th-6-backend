package com.runky.running.config;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.TokenRequiredException;
import com.runky.global.security.auth.MemberPrincipal;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	public static final String WS_AUTH_ATTR = "WS_AUTHENTICATION";

	// JwtCookieAuthFilter와 동일한 상수
	private static final String ACCESS_TOKEN_COOKIE = "accessToken";
	private static final String HDR_AUTHORIZATION = "Authorization";
	private static final String HDR_X_ACCESS_TOKEN = "X-Access-Token";
	private static final String BEARER_PREFIX = "Bearer ";

	private final TokenDecoder tokenDecoder;

	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
		@NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {

		if (request instanceof ServletServerHttpRequest servlet) {
			HttpServletRequest req = servlet.getServletRequest();

			// 1) 쿠키 → 2) 헤더 순으로 토큰 조회 (필터와 동일한 우선순위)
			String token = resolveFromCookie(req.getCookies());
			if (!StringUtils.hasText(token)) {
				token = resolveFromHeader(req);
			}

			if (StringUtils.hasText(token)) {
				try {
					AccessTokenClaims claims = tokenDecoder.decodeAccess(token);
					MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());
					Authentication auth = new UsernamePasswordAuthenticationToken(
						principal, null, principal.authorities());
					attributes.put(WS_AUTH_ATTR, auth); // 핸드셰이크 세션에 저장(폴백 용도)
				} catch (ExpiredTokenException e) {
					log.debug("[WS][HANDSHAKE] expired token: {}", e.getMessage());
				} catch (TokenRequiredException e) {
					log.debug("[WS][HANDSHAKE] invalid token: {}", e.getMessage());
				} catch (Exception e) {
					log.warn("[WS][HANDSHAKE] token decode error", e);
				}
			}
		}
		// 핸드셰이크는 관대하게 통과 → 최종 거부는 STOMP CONNECT에서 수행
		return true;
	}

	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
		@NonNull WebSocketHandler wsHandler, Exception exception) {
	}

	private String resolveFromCookie(Cookie[] cookies) {
		if (cookies == null)
			return null;
		return Arrays.stream(cookies)
			.filter(c -> Objects.equals(ACCESS_TOKEN_COOKIE, c.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

	private String resolveFromHeader(HttpServletRequest req) {
		String a = req.getHeader(HDR_AUTHORIZATION);
		if (StringUtils.hasText(a) && a.startsWith(BEARER_PREFIX)) {
			return a.substring(BEARER_PREFIX.length()).trim();
		}
		String x = req.getHeader(HDR_X_ACCESS_TOKEN);
		return StringUtils.hasText(x) ? x.trim() : null;
	}
}
