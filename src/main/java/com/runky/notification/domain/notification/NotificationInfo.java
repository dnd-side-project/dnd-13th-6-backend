package com.runky.notification.domain.notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class NotificationInfo {
	private NotificationInfo() {
	}

	public record Summary(
		Long id, String title, String message, Long senderId, boolean read,
		Instant createdAt, NotificationTemplate template, Map<String, String> variables
	) {
		public static Summary from(Notification n) {
			return new Summary(
				n.getId(), n.getTitle(), n.getMessage(), n.getSenderId(), n.isRead(),
				n.getCreatedAt().toInstant(), n.getTemplate(), n.getVariables()
			);
		}
	}

	public record Summaries(List<Summary> values) {
	}

}
