package com.runky.notification.domain.push;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushSendService {

	private final PushSender pushSender;

	public PushSender.SendResult sendToOne(String token, String title, String body, Map<String, String> data) {
		return pushSender.sendUnicast(token, new PushSender.NotificationPayload(title, body, data));
	}

	public PushSender.SendResult sendToMany(List<String> tokens, String title, String body, Map<String, String> data) {
		return pushSender.sendMulticast(tokens, new PushSender.NotificationPayload(title, body, data));
	}
}
