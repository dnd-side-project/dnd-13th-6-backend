package com.runky.running.infra.websocket.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 세션 관리자 - Map<UserId, SessionId> 구조로 사용자별 세션 추적 - 재연결 시 이전 세션 즉시 만료 및 리소스 반환
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();
    private final Map<String, SessionMetadata> sessionMetadataMap = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketSessionManager(@Lazy SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 사용자 연결 등록 재연결 시 이전 세션을 즉시 만료시킴
     */
    public void registerSession(Long userId, String sessionId) {
        String previousSessionId = userSessionMap.get(userId);

        if (previousSessionId != null && !previousSessionId.equals(sessionId)) {
            log.warn("[SessionManager]  재연결 감지 - userId={}, oldSessionId={}, newSessionId={}",
                    userId, previousSessionId, sessionId);
            expireSession(userId, previousSessionId);
        }

        userSessionMap.put(userId, sessionId);
        sessionMetadataMap.put(sessionId, new SessionMetadata(userId, System.currentTimeMillis()));

        log.info("[SessionManager] 세션 등록 완료 - userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 세션 제거
     */
    public void unregisterSession(String sessionId) {
        SessionMetadata metadata = sessionMetadataMap.remove(sessionId);

        if (metadata != null) {
            // 현재 세션이 최신 세션인 경우에만 userSessionMap에서 제거
            userSessionMap.compute(metadata.userId(), (userId, currentSessionId) -> {
                if (sessionId.equals(currentSessionId)) {
                    log.info("[SessionManager] 세션 제거 완료 - userId={}, sessionId={}, 유지시간={}ms",
                            userId, sessionId, System.currentTimeMillis() - metadata.connectedAt());
                    return null; // 제거
                } else {
                    log.debug("[SessionManager] 이미 새 세션으로 교체됨 - userId={}, oldSessionId={}, currentSessionId={}",
                            userId, sessionId, currentSessionId);
                    return currentSessionId; // 유지
                }
            });
        }
    }

    /**
     * 사용자의 현재 세션 ID 조회
     */
    public Optional<String> getSessionId(Long userId) {
        return Optional.ofNullable(userSessionMap.get(userId));
    }

    /**
     * 세션의 사용자 ID 조회
     */
    public Optional<Long> getUserId(String sessionId) {
        return Optional.ofNullable(sessionMetadataMap.get(sessionId))
                .map(SessionMetadata::userId);
    }

    /**
     * 특정 사용자에게 메시지 전송
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        getSessionId(userId).ifPresentOrElse(
                sessionId -> {
                    String userDestination = "/user/" + userId + destination;
                    messagingTemplate.convertAndSend(userDestination, payload);
                    log.debug("[SessionManager] 사용자 메시지 전송 - userId={}, destination={}", userId, userDestination);
                },
                () -> log.warn("[SessionManager] 활성 세션 없음 - userId={}", userId)
        );
    }


    private void expireSession(Long userId, String oldSessionId) {
        log.warn("[SessionManager] 좀비 세션 만료 시작 - userId={}, oldSessionId={}", userId, oldSessionId);

        // 1. 클라이언트에게 세션 만료 알림 전송
        try {
            // ERROR 프레임을 보내서 클라이언트가 연결을 끊도록 유도
            messagingTemplate.convertAndSendToUser(
                    oldSessionId,
                    "/queue/session-expired",
                    new SessionExpiredMessage("DUPLICATE_SESSION", "새로운 연결이 감지되어 이전 세션이 종료되었습니다.")
            );
            log.info("[SessionManager] 만료 알림 전송 완료 - sessionId={}", oldSessionId);
        } catch (Exception e) {
            log.warn("[SessionManager] 만료 알림 전송 실패 (이미 끊긴 세션) - sessionId={}, error={}",
                    oldSessionId, e.getMessage());
        }

        // 2. 메모리에서 세션 메타데이터 즉시 제거 (클라이언트 응답 여부와 무관)
        SessionMetadata metadata = sessionMetadataMap.remove(oldSessionId);
        if (metadata != null) {
            long duration = System.currentTimeMillis() - metadata.connectedAt();
            log.info("[SessionManager] 🗑️ 메타데이터 제거 완료 - sessionId={}, 유지시간={}ms",
                    oldSessionId, duration);
        } else {
            log.warn("[SessionManager]  세션 메타데이터 없음 (이미 제거됨) - sessionId={}", oldSessionId);
        }

        // 3. userSessionMap은 registerSession()에서 새 세션으로 덮어씌워지므로 여기서는 처리 불필요

        log.info("[SessionManager] 좀비 세션 만료 완료 - userId={}, oldSessionId={}", userId, oldSessionId);
    }

    /**
     * 현재 활성 세션 수
     */
    public int getActiveSessionCount() {
        return userSessionMap.size();
    }

    /**
     * 세션 메타데이터
     */
    private record SessionMetadata(Long userId, long connectedAt) {
    }

    /**
     * 세션 만료 메시지
     */
    public record SessionExpiredMessage(String code, String reason) {
    }
}
