package com.runky.notification.domain.push;

import static com.runky.notification.domain.push.PushInfo.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.notification.NotificationCommand;
import com.runky.notification.domain.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushService {

	private final DeviceTokenService deviceTokenService;
	private final PushSendService pushSendService;
	private final NotificationService notificationService;

	@Transactional
	public SenTPush.Summary pushToOne(PushCommand.Push.ToOne command) {
		String token = deviceTokenService.getActiveToken(new PushCommand.DeviceToken.Push.Get(command.receiverId()))
			.token();
		PushSender.SendResult sendResult = pushSendService.sendToOne(token, command.title(), command.body(),
			command.data());
		notificationService.record(
			new NotificationCommand.Record(command.senderId(), command.receiverId(), command.title(), command.body()));
		return new SenTPush.Summary(sendResult.success(), sendResult.failure(), sendResult.invalidTokens());

	}

	@Transactional
	public SenTPush.Summary pushToMany(PushCommand.Push.ToMany command) {
		List<String> tokens = deviceTokenService.getActiveTokens(
				new PushCommand.DeviceToken.Push.Gets(command.receiverIds()))
			.tokens();
		PushSender.SendResult sendResult = pushSendService.sendToMany(tokens, command.title(), command.body(),
			command.data());
		notificationService.records(
			new NotificationCommand.Records(command.senderId(), command.receiverIds(), command.title(),
				command.body()));
		return new SenTPush.Summary(sendResult.success(), sendResult.failure(), sendResult.invalidTokens());
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
