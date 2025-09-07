package com.runky.notification.api;

import com.runky.notification.application.NotificationCriteria;

public sealed interface DeviceTokenRequest {

	record Register(String token, String deviceType) implements DeviceTokenRequest {
		public NotificationCriteria.RegisterDeviceToken toCriteria(Long memberId) {
			return new NotificationCriteria.RegisterDeviceToken(memberId, token, deviceType);
		}
	}

	record Delete(String token) implements DeviceTokenRequest {
		public NotificationCriteria.DeleteDeviceToken toCriteria(Long memberId) {
			return new NotificationCriteria.DeleteDeviceToken(memberId, token);

		}
	}

}
