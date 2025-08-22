package com.runky.notification.domain.push;

import java.util.List;

public sealed interface DeviceTokenInfo {

	record DeletionResult(int count) implements DeviceTokenInfo {
	}

	record ActiveToken(String token) implements DeviceTokenInfo {

	}

	record ActiveTokens(List<String> tokens) implements DeviceTokenInfo {

	}

	record ExistenceCheck(boolean exists) implements DeviceTokenInfo {

	}

}
