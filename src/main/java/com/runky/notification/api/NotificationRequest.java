package com.runky.notification.api;

import com.runky.notification.application.NotificationCriteria;

public sealed interface NotificationRequest {

	record GetRecentTopN(Long receiverId, int limit) implements NotificationRequest {
		public NotificationCriteria.GetRecentTopN toCriteria() {
			return new NotificationCriteria.GetRecentTopN(receiverId, limit);
		}
	}
}
