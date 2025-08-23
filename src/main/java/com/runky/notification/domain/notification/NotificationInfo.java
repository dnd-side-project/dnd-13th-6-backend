package com.runky.notification.domain.notification;

import java.time.Instant;
import java.util.List;

public interface NotificationInfo {
	record Summary(
		Long id,
		String title,
		String message,
		Long senderId,
		boolean read,
		Instant createdAt
	) {
	}

	record Summaries(List<Summary> values) {

	}

}
