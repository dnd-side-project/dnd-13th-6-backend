package com.runky.running.infra.websocket.heartbeat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Heartbeat 모니터링 컴포넌트
 * - 일정 시간 Heartbeat가 없으면 세션 이상 감지
 * - 서버 측에서 연결 상태 추적
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatMonitor {

	private final SimpMessagingTemplate messagingTemplate;
	private final Map<String, Long> sessionHeartbeatMap = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private static final long HEARTBEAT_TIMEOUT_MS = 30_000; // 30초
	private static final long CHECK_INTERVAL_MS = 10_000; // 10초마다 체크

	@PostConstruct
	public void startMonitoring() {
		scheduler.scheduleAtFixedRate(
			this::checkHeartbeats,
			CHECK_INTERVAL_MS,
			CHECK_INTERVAL_MS,
			TimeUnit.MILLISECONDS
		);
		log.info("[HeartbeatMonitor] 모니터링 시작 - timeout={}ms, interval={}ms",
			HEARTBEAT_TIMEOUT_MS, CHECK_INTERVAL_MS);
	}

	@PreDestroy
	public void stopMonitoring() {
		scheduler.shutdown();
		log.info("[HeartbeatMonitor] 모니터링 종료");
	}

	/**
	 * Heartbeat 수신 기록
	 */
	public void recordHeartbeat(String sessionId) {
		sessionHeartbeatMap.put(sessionId, System.currentTimeMillis());
		log.debug("[HeartbeatMonitor] Heartbeat 수신 - sessionId={}", sessionId);
	}

	/**
	 * 세션 제거
	 */
	public void removeSession(String sessionId) {
		sessionHeartbeatMap.remove(sessionId);
		log.debug("[HeartbeatMonitor] 세션 제거 - sessionId={}", sessionId);
	}

	/**
	 * Heartbeat 타임아웃 체크
	 */
	private void checkHeartbeats() {
		long now = System.currentTimeMillis();

		sessionHeartbeatMap.forEach((sessionId, lastHeartbeat) -> {
			long elapsed = now - lastHeartbeat;

			if (elapsed > HEARTBEAT_TIMEOUT_MS) {
				log.warn("[HeartbeatMonitor] Heartbeat 타임아웃 - sessionId={}, elapsed={}ms",
					sessionId, elapsed);

				// 클라이언트에게 재연결 권장 메시지 전송 (선택적)
				try {
					messagingTemplate.convertAndSendToUser(
						sessionId,
						"/queue/reconnect",
						new ReconnectSuggestion("Heartbeat timeout detected. Please reconnect.")
					);
				} catch (Exception e) {
					log.error("[HeartbeatMonitor] 재연결 메시지 전송 실패 - sessionId={}", sessionId, e);
				}

				sessionHeartbeatMap.remove(sessionId);
			}
		});
	}

	/**
	 * 재연결 권장 메시지
	 */
	public record ReconnectSuggestion(String message) {
	}
}
