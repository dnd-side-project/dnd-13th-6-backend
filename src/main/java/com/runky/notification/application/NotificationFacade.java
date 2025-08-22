package com.runky.notification.application;

import static com.runky.notification.application.NotificationResult.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.push.DeviceTokenCommand;
import com.runky.notification.domain.push.DeviceTokenInfo;
import com.runky.notification.domain.push.DeviceTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationFacade {

	private final DeviceTokenService deviceTokenService;

	@Transactional
	public void registerDeviceToken(NotificationCriteria.RegisterDeviceToken criteria) {
		deviceTokenService.register(new DeviceTokenCommand.Register(criteria.memberId(), criteria.token(),
			criteria.deviceType()));
	}

	@Transactional
	public DeviceTokenDeletionResult deleteDeviceToken(NotificationCriteria.DeleteDeviceToken criteria) {
		DeviceTokenInfo.DeletionResult info = deviceTokenService.delete(
			new DeviceTokenCommand.Delete(criteria.memberId(), criteria.token()));

		return new DeviceTokenDeletionResult(info.count());
	}
}
