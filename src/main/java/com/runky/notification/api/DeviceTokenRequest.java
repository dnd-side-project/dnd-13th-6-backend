package com.runky.notification.api;

import com.runky.notification.application.DeviceTokenCriteria;

public sealed interface DeviceTokenRequest {

	record Register(String token, String deviceType) implements DeviceTokenRequest {
		DeviceTokenCriteria.Register toCriteria(Long memberId) {
			return new DeviceTokenCriteria.Register(memberId, token, deviceType);
		}
	}

	record Delete(String token) implements DeviceTokenRequest {
		DeviceTokenCriteria.Delete toCriteria(Long memberId) {
			return new DeviceTokenCriteria.Delete(memberId, token);

		}
	}

}
