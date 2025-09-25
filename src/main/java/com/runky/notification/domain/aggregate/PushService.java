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
	public PushSummary pushToOne(PushCommand.NotifyToOne command) {
		String token = deviceTokenService.getActiveToken(new PushCommand.GetDeviceToken(command.receiverId())).token();

		NotificationMessage notificationMessage = command.args();
		NotificationTemplate template = templateCatalog.resolve(notificationMessage.type());
		String title = template.title();
		String body = template.renderText(notificationMessage.variables());

		var send = pushSendService.sendToOne(token, title, body);

		notificationService.recordsByTemplate(
			new NotificationCommand.RecordByTemplate(
				command.senderId(), command.receiverId(), template, notificationMessage.variables()
			)
		);
		return new PushSummary(send.success(), send.failure(), send.invalidTokens());
	}

	@Transactional
	public PushSummary pushToMany(PushCommand.NotifyToMany command) {
		List<String> tokens = deviceTokenService.getActiveTokens(
			new PushCommand.GetAllDeviceToken(command.receiverIds())).tokens();

		NotificationMessage notificationMessage = command.args();
		NotificationTemplate template = templateCatalog.resolve(notificationMessage.type());
		String title = template.title();
		String body = template.renderText(notificationMessage.variables());

		var send = pushSendService.sendToMany(tokens, title, body);

		notificationService.recordsByTemplate(
			new NotificationCommand.RecordsByTemplate(
				command.senderId(), command.receiverIds(), template, notificationMessage.variables()
			)
		);
		return new PushSummary(send.success(), send.failure(), send.invalidTokens());
	}

	@Transactional
	public void registerDeviceToken(PushCommand.RegisterDeviceToken command) {
		deviceTokenService.register(new PushCommand.RegisterDeviceToken(command.memberId(), command.token(),
			command.deviceType()));
	}

	@Transactional
	public DeletionDTResult deleteDeviceToken(PushCommand.DeleteDeviceToken command) {
		DeletionDTResult info = deviceTokenService.delete(
			new PushCommand.DeleteDeviceToken(command.memberId(), command.token()));

		return new DeletionDTResult(info.count());
	}

}
