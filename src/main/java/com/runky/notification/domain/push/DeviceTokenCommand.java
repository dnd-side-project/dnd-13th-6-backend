package com.runky.notification.domain.push;

public sealed interface DeviceTokenCommand {

	// Command //
	record Register(Long memberId, String token, String deviceType) implements DeviceTokenCommand {
	}

	record Delete(Long memberId, String token) implements DeviceTokenCommand {
	}

	// Query //
	record Get(Long memberId, String deviceType) implements DeviceTokenCommand {
	}

	record Existence(Long memberId, String deviceType) implements DeviceTokenCommand {
	}
}
