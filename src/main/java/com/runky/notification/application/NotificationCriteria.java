package com.runky.notification.application;

import java.util.List;
import java.util.Map;

public sealed interface NotificationCriteria {

	record RegisterDeviceToken(Long memberId, String token, String deviceType) implements NotificationCriteria {

	}

	record DeleteDeviceToken(Long memberId, String token) implements NotificationCriteria {

	}

	record PushToOne(Long memberId, String title, String body, Map<String, String> data)
		implements NotificationCriteria {

	}

	record PushToMany(List<Long> memberIds, String title, String body, Map<String, String> data)
		implements NotificationCriteria {
	}
}
