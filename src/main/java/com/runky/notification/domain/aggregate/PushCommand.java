package com.runky.notification.domain.aggregate;

import java.util.List;
import java.util.Map;

import com.runky.notification.domain.notification.NotificationMessage;

public final class PushCommand {
	private PushCommand() {
	}

	public record RegisterDeviceToken(Long memberId, String token, String deviceType) {
	}

	public record DeleteDeviceToken(Long memberId, String token) {
	}

	public record GetDeviceToken(Long memberId) {
	}

	public record GetAllDeviceToken(List<Long> memberIds) {
	}

	public record CheckDTExistence(Long memberId) {
	}

	public record NotifyToOne(Long senderId, Long receiverId, NotificationMessage args, Map<String, String> data) {
	}

	public record NotifyToMany(Long senderId, List<Long> receiverIds, NotificationMessage args,
							   Map<String, String> data) {
	}

}
