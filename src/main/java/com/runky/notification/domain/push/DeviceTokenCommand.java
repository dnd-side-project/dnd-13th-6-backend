package com.runky.notification.domain.push;

import java.util.List;

public sealed interface DeviceTokenCommand {

	// Command //
	record Register(Long memberId, String token, String deviceType) implements DeviceTokenCommand {
	}

	record Delete(Long memberId, String token) implements DeviceTokenCommand {
	}

	// Query //
	record Get(Long memberId) implements DeviceTokenCommand {
	}

	record Gets(List<Long> memberIds) implements DeviceTokenCommand {
	}

	record CheckExistence(Long memberId) implements DeviceTokenCommand {
	}
}
