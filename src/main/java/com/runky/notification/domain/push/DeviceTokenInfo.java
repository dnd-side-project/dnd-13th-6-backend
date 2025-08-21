package com.runky.notification.domain.push;

public sealed interface DeviceTokenInfo {

	record Delete(int count) implements DeviceTokenInfo {
	}

	record View(Long id, Long memberId, String token, boolean active) implements DeviceTokenInfo {

	}

	record Existence(boolean exists) implements DeviceTokenInfo {

	}

}
