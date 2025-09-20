package com.runky.notification.application;

public final class NotificationCriteria {

	public record RegisterDeviceToken(Long memberId, String token, String deviceType) {
	}

	public record DeleteDeviceToken(Long memberId, String token) {

	}

	public record GetRecentTopN(Long receiverId, int limit) {
	}

}
