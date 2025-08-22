package com.runky.notification.application;

import static com.runky.notification.application.NotificationResult.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.push.PushCommand;
import com.runky.notification.domain.push.PushService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationFacade {

	private final PushService pushService;

	@Transactional
	public void registerDeviceToken(NotificationCriteria.RegisterDeviceToken criteria) {
		var registerInfo = new PushCommand.DeviceToken.Register(criteria.memberId(), criteria.token(),
			criteria.deviceType());

		pushService.registerDeviceToken(registerInfo);
	}

	@Transactional
	public DeviceTokenDeletionResult deleteDeviceToken(NotificationCriteria.DeleteDeviceToken criteria) {
		var deleteInfo = new PushCommand.DeviceToken.Delete(criteria.memberId(), criteria.token());
		int count = pushService.deleteDeviceToken(deleteInfo).count();
		return new DeviceTokenDeletionResult(count);
	}

	@Transactional
	public PushResult pushToOne(NotificationCriteria.PushToOne criteria) {
		var pushToOneInfo = new PushCommand.Push.ToOne(criteria.memberId(), criteria.title(), criteria.body(),
			criteria.data());
		var result = pushService.pushToOne(pushToOneInfo);
		return new PushResult(result.success(), result.failure(), result.invalidTokens());
	}

	@Transactional
	public PushResult pushToMany(NotificationCriteria.PushToMany criteria) {
		var pushToOneInfo = new PushCommand.Push.ToMany(criteria.memberIds(), criteria.title(), criteria.body(),
			criteria.data());
		var result = pushService.pushToMany(pushToOneInfo);
		return new PushResult(result.success(), result.failure(), result.invalidTokens());
	}
}
