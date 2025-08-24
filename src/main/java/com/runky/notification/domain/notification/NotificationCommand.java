package com.runky.notification.domain.notification;

import java.util.List;
import java.util.Map;

public sealed interface NotificationCommand {

	record RecordByTemplate(Long senderId, Long receiverId,
							NotificationTemplate template, Map<NotificationTemplate.VarKey, String> variables)
		implements NotificationCommand {

	}

	record RecordsByTemplate(Long senderId, List<Long> receiverIds,
							 NotificationTemplate template, Map<NotificationTemplate.VarKey, String> variables)
		implements NotificationCommand {
	}

	record GetRecentTopN(Long receiverId, int limit) implements NotificationCommand {
	}

}
