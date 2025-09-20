package com.runky.notification.api;

import com.runky.notification.application.NotificationCriteria;

public final class NotificationRequest {
	private NotificationRequest() {
	}

	public record GetRecentTopN(Long receiverId, int limit) {
		public NotificationCriteria.GetRecentTopN toCriteria() {
			return new NotificationCriteria.GetRecentTopN(receiverId, limit);
		}
	}
}
