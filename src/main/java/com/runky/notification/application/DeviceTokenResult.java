package com.runky.notification.application;

public sealed interface DeviceTokenResult {

	record Delete(int count) implements DeviceTokenResult {
	}

}
