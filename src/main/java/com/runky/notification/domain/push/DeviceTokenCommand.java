package com.runky.notification.domain.push;

public sealed interface DeviceTokenCommand {

	// Command //
	record Register(Long memberId, String token) implements DeviceTokenCommand {
	}

	record Delete(Long memberId, String token) implements DeviceTokenCommand {
	}

	// Query //
	record Find(Long memberId, String token) implements DeviceTokenCommand {
	}
}
