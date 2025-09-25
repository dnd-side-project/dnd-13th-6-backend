package com.runky.notification.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.runky.notification.domain.aggregate.PushCommand;
import com.runky.notification.domain.notification.NotificationCommand;
import com.runky.notification.domain.notification.NotificationMessage;
import com.runky.notification.domain.notification.NotificationService;
import com.runky.notification.domain.notification.NotificationTemplate;
import com.runky.notification.domain.notification.TemplateCatalog;
import com.runky.notification.domain.push.DeviceTokenService;
import com.runky.notification.domain.push.PushSendService;
import com.runky.notification.interfaces.consumer.NotificationEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushCommandHandler {
	private final DeviceTokenService deviceTokenService;
	private final PushSendService pushSendService;
	private final NotificationService notificationService;
	private final TemplateCatalog templateCatalog;

	public void pushToOne(NotificationEvent.NotifyToOne command) {
		String token = deviceTokenService.getActiveToken(new PushCommand.GetDeviceToken(command.receiverId())).token();

		NotificationMessage notificationMessage = command.args();
		NotificationTemplate template = templateCatalog.resolve(notificationMessage.type());
		String title = template.title();
		String body = template.renderText(notificationMessage.variables());

		pushSendService.sendToOne(token, title, body);

		notificationService.recordsByTemplate(
			new NotificationCommand.RecordByTemplate(
				command.senderId(), command.receiverId(), template, notificationMessage.variables()
			)
		);
	}

	public void pushToMany(NotificationEvent.NotifyToMany command) {
		List<String> tokens = deviceTokenService.getActiveTokens(
			new PushCommand.GetAllDeviceToken(command.receiverIds())).tokens();

		NotificationMessage notificationMessage = command.args();
		NotificationTemplate template = templateCatalog.resolve(notificationMessage.type());
		String title = template.title();
		String body = template.renderText(notificationMessage.variables());

		pushSendService.sendToMany(tokens, title, body);

		notificationService.recordsByTemplate(
			new NotificationCommand.RecordsByTemplate(
				command.senderId(), command.receiverIds(), template, notificationMessage.variables()
			)
		);
	}
}
