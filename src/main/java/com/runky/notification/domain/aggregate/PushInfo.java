package com.runky.notification.domain.aggregate;

import java.util.List;

public final class PushInfo {
	private PushInfo() {
	}

	public record DeletionDTResult(int count) {
	}

	public record ActiveDeviceToken(String token) {
	}

	public record ActiveDeviceTokens(List<String> tokens) {
	}

	public record DeviceExistenceCheck(boolean exists) {
	}

	public record PushSummary(int success, int failure, List<String> invalidTokens) {
	}

}
