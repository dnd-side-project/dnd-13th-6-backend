package com.runky.notification.application;

import java.time.Instant;
import java.util.List;

public sealed interface NotificationResult {

	record DeviceTokenDeletionResult(int count) implements NotificationResult {
	}

	/** 애플리케이션 레벨 조회 요약 */
	record Summary(
		Long id,
		String title,
		String message,
		Long senderId,
		boolean read,
		Instant createdAt
	) {
	}

	record Items(List<Summary> values) {
	}
}
