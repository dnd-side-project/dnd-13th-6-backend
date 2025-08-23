package com.runky.notification.domain.notification;

import java.util.List;

public sealed interface NotificationCommand {

	record Record(Long senderId, Long receiverId, String title, String message) implements NotificationCommand {

	}

	record Records(Long senderId, List<Long> receiverIds, String title, String message) implements NotificationCommand {
	}
}
