package com.runky.notification.application;

public sealed interface NotificationCriteria {

	record RegisterDeviceToken(Long memberId, String token, String deviceType) implements NotificationCriteria {

	}

	record DeleteDeviceToken(Long memberId, String token) implements NotificationCriteria {

	}

	record GetRecentTopN(Long receiverId, int limit) implements NotificationCriteria {
	}

}
