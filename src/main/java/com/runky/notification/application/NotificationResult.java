package com.runky.notification.application;

public sealed interface NotificationResult {

	record DeviceTokenDeletionResult(int count) implements NotificationResult {
	}

}
