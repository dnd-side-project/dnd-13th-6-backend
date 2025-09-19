package com.runky.running.infra.websocket.handshake;

import static com.runky.running.constants.RunningSocketConstants.*;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.runky.running.infra.websocket.auth.CookieBasedWebSocketAuthenticator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieAuthHandshakeInterceptor implements HandshakeInterceptor {

	private final CookieBasedWebSocketAuthenticator cookieAuthenticator;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) {
		try {
			Authentication auth = cookieAuthenticator.authenticate(request);
			attributes.put(AUTHENTICATION, auth);
			return true;
		} catch (Exception e) {
			log.warn("[WS][Handshake] Authentication failed", e);
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
		WebSocketHandler wsHandler, Exception ex) {
	}
}
