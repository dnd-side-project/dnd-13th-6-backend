package com.runky.notification.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public sealed interface NotificationResult {

	record DeviceTokenDeletionResult(int count) implements NotificationResult {
	}

	record Message(
		String type,
		String raw,
		Map<String, String> variables
	) implements NotificationResult {
	}

	/** 애플리케이션 레벨 조회 요약 */
	record Summary(
		Long id,
		String title,
		String text,
		Long senderId,
		boolean read,
		Instant createdAt,
		Message message

	) {
	}

	record Items(List<Summary> values) {
	}
}
