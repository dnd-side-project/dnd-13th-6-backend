package com.runky.notification.interfaces;

import com.runky.notification.application.NotificationCriteria;

public final class DeviceTokenRequest {
	private DeviceTokenRequest() {
	}

	public record Register(String token, String deviceType) {
		public NotificationCriteria.RegisterDeviceToken toCriteria(Long memberId) {
			return new NotificationCriteria.RegisterDeviceToken(memberId, token, deviceType);
		}
	}

	public record Delete(String token) {
		public NotificationCriteria.DeleteDeviceToken toCriteria(Long memberId) {
			return new NotificationCriteria.DeleteDeviceToken(memberId, token);
		}
	}

}
