package com.runky.notification.domain.push;

import java.util.List;
import java.util.Map;

public interface PushSender {

	SendResult sendUnicast(String token, NotificationPayload payload);

	SendResult sendMulticast(List<String> tokens, NotificationPayload payload);

	record NotificationPayload(String title, String body, Map<String, String> data) {
		public static NotificationPayload of(String title, String body) {
			return new NotificationPayload(title, body, Map.of());
		}
	}

	record SendResult(int success, int failure, List<String> invalidTokens) {
		public int total() {
			return success + failure;
		}
	}
}
