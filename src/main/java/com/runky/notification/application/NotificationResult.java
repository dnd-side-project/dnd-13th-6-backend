package com.runky.notification.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class NotificationResult {
	private NotificationResult() {
	}

	public record DeviceTokenDeletionResult(int count) {
	}

	public record Message(String type, String raw, Map<String, String> variables) {
	}

	public record Summary(
		Long id, String title, String text,
		Long senderId, boolean read, Instant createdAt,
		Message message
	) {
	}

	public record Items(List<Summary> values) {
	}
}
