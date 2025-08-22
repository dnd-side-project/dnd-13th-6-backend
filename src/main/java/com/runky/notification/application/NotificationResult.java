package com.runky.notification.application;

import java.util.List;

public sealed interface NotificationResult {

	record DeviceTokenDeletionResult(int count) implements NotificationResult {
	}

	record PushResult(int success, int failure, List<String> invalidTokens) implements NotificationResult {
	}

}
