package com.runky.notification.domain.notification;

import java.util.List;
import java.util.Map;

public final class NotificationCommand {
	private NotificationCommand() {
	}

	public record RecordByTemplate(Long senderId, Long receiverId,
								   NotificationTemplate template, Map<NotificationTemplate.VarKey, String> variables) {

	}

	public record RecordsByTemplate(Long senderId, List<Long> receiverIds,
									NotificationTemplate template, Map<NotificationTemplate.VarKey, String> variables) {
	}

	public record GetRecentTopN(Long receiverId, int limit) {
	}

}
