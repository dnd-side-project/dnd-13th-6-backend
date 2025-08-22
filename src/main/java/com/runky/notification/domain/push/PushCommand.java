package com.runky.notification.domain.push;

import java.util.List;
import java.util.Map;

public sealed interface PushCommand {

	sealed interface Push extends PushCommand {
		record ToOne(Long memberId, String title, String body, Map<String, String> data) implements Push {

		}

		record ToMany(List<Long> memberIds, String title, String body, Map<String, String> data) implements Push {

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
