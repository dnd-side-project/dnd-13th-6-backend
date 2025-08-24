package com.runky.notification.domain.aggregate;

import static com.runky.notification.domain.aggregate.PushInfo.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.notification.NotificationCommand;
import com.runky.notification.domain.notification.NotificationMessage;
import com.runky.notification.domain.notification.NotificationService;
import com.runky.notification.domain.notification.NotificationTemplate;
import com.runky.notification.domain.notification.TemplateCatalog;
import com.runky.notification.domain.push.DeviceTokenService;
import com.runky.notification.domain.push.PushSendService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushService {

	private final DeviceTokenService deviceTokenService;
	private final PushSendService pushSendService;
	private final NotificationService notificationService;
	private final TemplateCatalog templateCatalog;

	@Transactional
	public SenTPush.Summary pushToOne(PushCommand.Notify.ToOne command) {
		String token = deviceTokenService.getActiveToken(new PushCommand.Get(command.receiverId())).token();

		NotificationMessage notificationMessage = command.args();
		NotificationTemplate template = templateCatalog.resolve(notificationMessage.type());
		String title = template.title();
		String body = template.renderText(notificationMessage.variables());

		var send = pushSendService.sendToOne(token, title, body, command.data());

		notificationService.recordsByTemplate(
			new NotificationCommand.RecordByTemplate(
				command.senderId(), command.receiverId(), template, notificationMessage.variables()
			)
		);
		return new SenTPush.Summary(send.success(), send.failure(), send.invalidTokens());
	}

	@Transactional
	public SenTPush.Summary pushToMany(PushCommand.Notify.ToMany command) {
		List<String> tokens = deviceTokenService.getActiveTokens(new PushCommand.Gets(command.receiverIds())).tokens();

		NotificationMessage notificationMessage = command.args();
		NotificationTemplate template = templateCatalog.resolve(notificationMessage.type());
		String title = template.title();
		String body = template.renderText(notificationMessage.variables());

		var send = pushSendService.sendToMany(tokens, title, body, command.data());

		notificationService.recordsByTemplate(
			new NotificationCommand.RecordsByTemplate(
				command.senderId(), command.receiverIds(), template, notificationMessage.variables()
			)
		);
		return new SenTPush.Summary(send.success(), send.failure(), send.invalidTokens());
	}

	@Transactional
	public void registerDeviceToken(PushCommand.DeviceToken.Register command) {
		deviceTokenService.register(new PushCommand.DeviceToken.Register(command.memberId(), command.token(),
			command.deviceType()));
	}

	@Transactional
	public DeviceTokenInfo.DeletionResult deleteDeviceToken(PushCommand.DeviceToken.Delete command) {
		DeviceTokenInfo.DeletionResult info = deviceTokenService.delete(
			new PushCommand.DeviceToken.Delete(command.memberId(), command.token()));

		return new DeviceTokenInfo.DeletionResult(info.count());
	}

}
