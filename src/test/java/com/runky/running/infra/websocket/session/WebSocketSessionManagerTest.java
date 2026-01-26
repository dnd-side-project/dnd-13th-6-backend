package com.runky.running.infra.websocket.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketSessionManager 테스트")
class WebSocketSessionManagerTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private WebSocketSessionManager sessionManager;

	@BeforeEach
	void setUp() {
		sessionManager = new WebSocketSessionManager(messagingTemplate);
	}

	@Test
	@DisplayName("세션 등록 - 신규 사용자")
	void registerSession_newUser() {
		// Given
		Long userId = 1L;
		String sessionId = "session-123";

		// When
		sessionManager.registerSession(userId, sessionId);

		// Then
		assertThat(sessionManager.getSessionId(userId))
			.isPresent()
			.hasValue(sessionId);
		assertThat(sessionManager.getUserId(sessionId))
			.isPresent()
			.hasValue(userId);
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);

		// 만료 알림이 전송되지 않아야 함
		verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
	}

	@Test
	@DisplayName("세션 등록 - 재연결 시 이전 세션 즉시 만료")
	void registerSession_reconnect_expiresPreviousSession() {
		// Given
		Long userId = 1L;
		String oldSessionId = "session-old";
		String newSessionId = "session-new";

		// 초기 연결
		sessionManager.registerSession(userId, oldSessionId);

		// When - 재연결
		sessionManager.registerSession(userId, newSessionId);

		// Then
		// 1. userSessionMap은 새 세션으로 교체됨
		assertThat(sessionManager.getSessionId(userId))
			.isPresent()
			.hasValue(newSessionId);

		// 2. 이전 세션 메타데이터는 제거됨
		assertThat(sessionManager.getUserId(oldSessionId)).isEmpty();

		// 3. 새 세션 메타데이터는 존재함
		assertThat(sessionManager.getUserId(newSessionId))
			.isPresent()
			.hasValue(userId);

		// 4. 활성 세션 수는 1개 유지 (이전 세션은 제거됨)
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);

		// 5. 이전 세션에 만료 알림 전송됨
		verify(messagingTemplate).convertAndSendToUser(
			eq(oldSessionId),
			eq("/queue/session-expired"),
			any(WebSocketSessionManager.SessionExpiredMessage.class)
		);
	}

	@Test
	@DisplayName("세션 제거 - 정상 제거")
	void unregisterSession_success() {
		// Given
		Long userId = 1L;
		String sessionId = "session-123";
		sessionManager.registerSession(userId, sessionId);

		// When
		sessionManager.unregisterSession(sessionId);

		// Then
		assertThat(sessionManager.getSessionId(userId)).isEmpty();
		assertThat(sessionManager.getUserId(sessionId)).isEmpty();
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("세션 제거 - 이미 재연결된 경우 새 세션 유지")
	void unregisterSession_afterReconnect_keepsNewSession() {
		// Given
		Long userId = 1L;
		String oldSessionId = "session-old";
		String newSessionId = "session-new";

		sessionManager.registerSession(userId, oldSessionId);
		sessionManager.registerSession(userId, newSessionId); // 재연결

		// When - 이전 세션의 DISCONNECT 이벤트가 늦게 도착
		sessionManager.unregisterSession(oldSessionId);

		// Then - 새 세션은 유지되어야 함
		assertThat(sessionManager.getSessionId(userId))
			.isPresent()
			.hasValue(newSessionId);
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("다중 재연결 시 항상 최신 세션만 유지")
	void registerSession_multipleReconnects() {
		// Given
		Long userId = 1L;
		String session1 = "session-1";
		String session2 = "session-2";
		String session3 = "session-3";

		// When
		sessionManager.registerSession(userId, session1);
		sessionManager.registerSession(userId, session2);
		sessionManager.registerSession(userId, session3);

		// Then
		assertThat(sessionManager.getSessionId(userId))
			.isPresent()
			.hasValue(session3);
		assertThat(sessionManager.getUserId(session1)).isEmpty();
		assertThat(sessionManager.getUserId(session2)).isEmpty();
		assertThat(sessionManager.getUserId(session3))
			.isPresent()
			.hasValue(userId);
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);

		// 만료 알림이 2번 전송됨 (session1, session2)
		verify(messagingTemplate, times(2)).convertAndSendToUser(
			anyString(),
			eq("/queue/session-expired"),
			any(WebSocketSessionManager.SessionExpiredMessage.class)
		);
	}

	@Test
	@DisplayName("여러 사용자 동시 접속")
	void registerSession_multipleUsers() {
		// Given
		Long user1 = 1L;
		Long user2 = 2L;
		Long user3 = 3L;
		String session1 = "session-1";
		String session2 = "session-2";
		String session3 = "session-3";

		// When
		sessionManager.registerSession(user1, session1);
		sessionManager.registerSession(user2, session2);
		sessionManager.registerSession(user3, session3);

		// Then
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(3);
		assertThat(sessionManager.getSessionId(user1)).hasValue(session1);
		assertThat(sessionManager.getSessionId(user2)).hasValue(session2);
		assertThat(sessionManager.getSessionId(user3)).hasValue(session3);
	}

	@Test
	@DisplayName("사용자에게 메시지 전송 - 활성 세션 존재")
	void sendToUser_activeSession() {
		// Given
		Long userId = 1L;
		String sessionId = "session-123";
		sessionManager.registerSession(userId, sessionId);

		String destination = "/queue/test";
		String message = "Hello";

		// When
		sessionManager.sendToUser(userId, destination, message);

		// Then
		verify(messagingTemplate).convertAndSend(eq("/user/1/queue/test"), eq(message));
	}

	@Test
	@DisplayName("사용자에게 메시지 전송 - 활성 세션 없음")
	void sendToUser_noActiveSession() {
		// Given
		Long userId = 999L; // 등록되지 않은 사용자

		// When
		sessionManager.sendToUser(userId, "/queue/test", "Hello");

		// Then - 메시지가 전송되지 않음
		verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
	}

	@Test
	@DisplayName("재연결 시나리오 - 네트워크 끊김 후 재접속")
	void reconnectScenario_networkDisconnect() throws InterruptedException {
		// Given
		Long userId = 1L;
		String session1 = "session-1";
		String session2 = "session-2";

		// 1. 초기 연결
		sessionManager.registerSession(userId, session1);
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);

		// 2. 네트워크 끊김으로 클라이언트가 재연결 시도
		Thread.sleep(10); // 시간차 시뮬레이션
		sessionManager.registerSession(userId, session2);

		// 3. 이전 세션의 DISCONNECT 이벤트가 늦게 도착
		sessionManager.unregisterSession(session1);

		// Then - 새 세션만 유지
		assertThat(sessionManager.getSessionId(userId)).hasValue(session2);
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);

		// 이전 세션에 만료 알림 전송됨
		verify(messagingTemplate).convertAndSendToUser(
			eq(session1),
			eq("/queue/session-expired"),
			any(WebSocketSessionManager.SessionExpiredMessage.class)
		);
	}

	@Test
	@DisplayName("동시성 테스트 - 여러 스레드에서 동시 등록")
	void concurrentRegistration() throws InterruptedException {
		// Given
		int threadCount = 100;
		Long userId = 1L;

		// When - 100개 스레드가 동시에 재연결 시도
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			final String sessionId = "session-" + i;
			threads[i] = new Thread(() -> sessionManager.registerSession(userId, sessionId));
			threads[i].start();
		}

		// 모든 스레드 완료 대기
		for (Thread thread : threads) {
			thread.join();
		}

		// Then - 활성 세션은 1개만 유지 (마지막 등록된 세션)
		assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
		assertThat(sessionManager.getSessionId(userId)).isPresent();
	}
}
