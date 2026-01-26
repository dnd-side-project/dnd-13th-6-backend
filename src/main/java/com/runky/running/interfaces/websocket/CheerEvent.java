package com.runky.running.interfaces.websocket;

import java.time.LocalDateTime;

/**
 * 응원 메시지 이벤트 (1:1 수신용)
 */
public record CheerEvent(
	String type,
	Long fromUserId,
	String fromUserNickname,
	String message,
	LocalDateTime timestamp
) {
	public static CheerEvent of(Long fromUserId, String fromUserNickname, String message) {
		return new CheerEvent("CHEER", fromUserId, fromUserNickname, message, LocalDateTime.now());
	}
}
