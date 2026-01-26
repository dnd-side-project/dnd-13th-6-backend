package com.runky.running.infra.websocket.event;

import static com.runky.running.constants.RunningSocketConstants.*;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.runky.global.security.auth.MemberPrincipal;
import com.runky.running.infra.websocket.session.WebSocketSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 이벤트 리스너
 * - CONNECT/DISCONNECT/SUBSCRIBE 이벤트 처리
 * - SessionManager와 연동하여 세션 생명주기 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final WebSocketSessionManager sessionManager;

	/**
	 * 연결 성공 시 세션 등록
	 */
	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();

		extractMemberId(accessor).ifPresent(memberId -> {
			sessionManager.registerSession(memberId, sessionId);
			log.info("[WebSocket Event] 연결 완료 - memberId={}, sessionId={}", memberId, sessionId);
		});
	}

	/**
	 * 연결 해제 시 세션 제거
	 */
	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();

		sessionManager.unregisterSession(sessionId);
		log.info("[WebSocket Event] 연결 해제 - sessionId={}", sessionId);
	}

	/**
	 * 구독 이벤트 처리 (선택적)
	 */
	@EventListener
	public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = accessor.getDestination();
		String sessionId = accessor.getSessionId();

		extractMemberId(accessor).ifPresent(memberId ->
			log.debug("[WebSocket Event] 구독 - memberId={}, sessionId={}, destination={}",
				memberId, sessionId, destination)
		);
	}

	/**
	 * 세션 속성에서 MemberId 추출
	 */
	private Optional<Long> extractMemberId(StompHeaderAccessor accessor) {
		return Optional.ofNullable(accessor.getSessionAttributes())
			.map(attrs -> attrs.get(MEMBER_PRINCIPAL))
			.filter(MemberPrincipal.class::isInstance)
			.map(MemberPrincipal.class::cast)
			.map(MemberPrincipal::memberId);
	}
}
