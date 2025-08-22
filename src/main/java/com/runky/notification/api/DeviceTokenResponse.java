package com.runky.notification.api;

public sealed interface DeviceTokenResponse {

	record Delete(int count) implements DeviceTokenResponse {
	}

}
