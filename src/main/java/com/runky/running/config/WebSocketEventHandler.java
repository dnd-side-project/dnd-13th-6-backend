package com.runky.running.config;

import static com.runky.running.api.RunningSocketConstants.*;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring이 제공하는 애플리케이션 이벤트는 Connect/Connected/Subscribe/Unsubscribe/Disconnect까지
 */
@Slf4j
@Component
public class WebSocketEventHandler {

	@EventListener
	public void handleWebSocketSessionConnect(SessionConnectEvent event) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
		Authentication authentication = (Authentication)Objects.requireNonNull(accessor).getUser();
		String username = authentication.getName();
		String userRole = authentication.getAuthorities()
			.stream()
			.findFirst()
			.map(GrantedAuthority::getAuthority)
			.orElse("UNKNOWN");

		Object userId = accessor.getSessionAttributes() != null ?
			accessor.getSessionAttributes().get(ATTR_MEMBER_ID) : null;

		log.info("WebSocket {}: username={}, role={}, userId={}",
			event.getClass().getSimpleName(), username, userRole, userId);
	}

	@EventListener(SessionConnectedEvent.class)
	public void handleWebSocketSessionConnected(SessionConnectedEvent event) {
		StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
		if (acc != null) {
			log.info("WS CONNECTED: sessionId={}, {}", acc.getSessionId(), who(acc));
		} else {
			log.info("WS CONNECTED");
		}
	}

	@EventListener
	public void handleWebSocketSessionSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
		if (acc == null) {
			log.info("WS SUBSCRIBE");
			return;
		}
		log.info("WS SUBSCRIBE: sessionId={}, subId={}, dest={}, {}",
			acc.getSessionId(), acc.getSubscriptionId(), acc.getDestination(), who(acc));
	}

	@EventListener
	public void handleWebSocketSessionUnsubscribe(SessionUnsubscribeEvent event) {
		StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
		if (acc == null) {
			log.info("WS UNSUBSCRIBE");
			return;
		}
		log.info("WS UNSUBSCRIBE: sessionId={}, subId={}, {}",
			acc.getSessionId(), acc.getSubscriptionId(), who(acc));
	}

	@EventListener
	public void handleWebSocketSessionDisconnected(SessionDisconnectEvent event) {
		// close status/코드까지 출력
		var status = event.getCloseStatus(); // may be null
		if (status != null) {
			log.info("WS DISCONNECT: sessionId={}, code={}, reason={}", event.getSessionId(), status.getCode(),
				status.getReason());
		} else {
			log.info("WS DISCONNECT: sessionId={}", event.getSessionId());
		}
	}

	private String who(StompHeaderAccessor acc) {
		Object memberId = acc.getSessionAttributes() != null ? acc.getSessionAttributes().get(ATTR_MEMBER_ID) : null;
		var user = acc.getUser();
		String roles = (user instanceof Authentication a)
			? a.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","))
			: "ANON";
		String username = (user instanceof Authentication a) ? a.getName() : "anonymous";
		return "userId=" + memberId + ", username=" + username + ", roles=" + roles;
	}
}
