package com.runky.notification.application;

import static com.runky.notification.application.NotificationResult.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.aggregate.PushCommand;
import com.runky.notification.domain.aggregate.PushService;
import com.runky.notification.domain.notification.NotificationCommand;
import com.runky.notification.domain.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationFacade {

	private final PushService pushService;
	private final NotificationService notificationService;

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

	@Transactional(readOnly = true)
	public NotificationResult.Items recentTopN(NotificationCriteria.GetRecentTopN criteria) {
		var info = notificationService.getRecentTopN(
			new NotificationCommand.GetRecentTopN(criteria.receiverId(), criteria.limit())
		);

		List<Summary> summaries = info.values().stream()
			.map(s -> new Summary(s.id(), s.title(), s.message(), s.senderId(), s.read(), s.createdAt()))
			.toList();
		return new NotificationResult.Items(summaries);
	}
}
