package com.runky.notification.domain.push;

import java.util.List;

public interface PushSender {

	SendResult sendUnicast(String token, NotificationPayload payload);

	SendResult sendMulticast(List<String> tokens, NotificationPayload payload);

	record NotificationPayload(String title, String body) {
		public static NotificationPayload of(String title, String body) {
			return new NotificationPayload(title, body);
		}
	}

	record SendResult(int success, int failure, List<String> invalidTokens) {
		public int total() {
			return success + failure;
		}
	}
}
