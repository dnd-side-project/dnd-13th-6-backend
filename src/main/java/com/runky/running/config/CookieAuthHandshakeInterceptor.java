// src/main/java/com/runky/running/config/CookieAuthHandshakeInterceptor.java
package com.runky.running.config;

import static com.runky.running.api.RunningSocketConstants.*;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.global.security.auth.MemberPrincipal;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieAuthHandshakeInterceptor implements HandshakeInterceptor {

	public static final String ATTR_WS_AUTH = "ws.auth";
	// 필터와 동일한 네이밍을 사용
	private static final String ACCESS_TOKEN_COOKIE = "accessToken";
	private final TokenDecoder tokenDecoder;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attrs) {
		try {
			String token = null;

			// 1) Servlet 쿠키에서 우선 조회
			if (request instanceof ServletServerHttpRequest sreq) {
				Cookie[] cookies = sreq.getServletRequest().getCookies();
				if (cookies != null) {
					for (Cookie c : cookies) {
						if (ACCESS_TOKEN_COOKIE.equals(c.getName()) && StringUtils.hasText(c.getValue())) {
							token = c.getValue();
							break;
						}
					}
				}
			}

			// 2) Fallback: Cookie 헤더 직접 파싱
			if (!StringUtils.hasText(token)) {
				String cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
				if (StringUtils.hasText(cookieHeader)) {
					for (String part : cookieHeader.split(";")) {
						String[] nv = part.trim().split("=", 2);
						if (nv.length == 2 && ACCESS_TOKEN_COOKIE.equals(nv[0].trim())) {
							token = nv[1].trim();
							break;
						}
					}
				}
			}

			// 3) 쿠키 없으면 아무 것도 하지 않고 통과 → CONNECT에서 헤더로 처리
			if (!StringUtils.hasText(token)) {
				return true;
			}

			// 4) 쿠키 토큰 디코딩 → 세션 속성에 저장
			AccessTokenClaims claims = tokenDecoder.decodeAccess(token);
			MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());
			Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());

			attrs.put(ATTR_WS_AUTH, auth);
			attrs.put(ATTR_MEMBER_ID, claims.memberId());

			return true;
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
		WebSocketHandler wsHandler, Exception ex) {
	}
}
