package com.runky.notification.domain.aggregate;

import java.util.List;
import java.util.Map;

import com.runky.notification.domain.notification.NotificationMessage;

public sealed interface PushCommand {

	sealed interface Notify extends PushCommand {
		record ToOne(
			Long senderId,
			Long receiverId,
			NotificationMessage args,
			Map<String, String> data
		) implements Notify {
		}

		record ToMany(
			Long senderId,
			List<Long> receiverIds,
			NotificationMessage args,
			Map<String, String> data
		) implements Notify {
		}
	}

	sealed interface DeviceToken extends PushCommand {
		record Register(Long memberId, String token, String deviceType) implements DeviceToken {
		}

		record Delete(Long memberId, String token) implements DeviceToken {
		}

	}

	// Query //
	record Get(Long memberId) implements DeviceToken {
	}

	record Gets(List<Long> memberIds) implements DeviceToken {
	}

	record CheckExistence(Long memberId) implements DeviceToken {
	}

}
