package com.runky.notification.application;

public sealed interface DeviceTokenCriteria {

	record Register(Long memberId, String token, String deviceType) implements DeviceTokenCriteria {

	}

	record Delete(Long memberId, String token) implements DeviceTokenCriteria {

	}
}
