package com.runky.notification.domain.push;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushSendService {

	private final PushSender pushSender;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public PushSender.SendResult sendToOne(String token, String title, String body) {
		return pushSender.sendUnicast(token, new PushSender.NotificationPayload(title, body));
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public PushSender.SendResult sendToMany(List<String> tokens, String title, String body) {
		return pushSender.sendMulticast(tokens, new PushSender.NotificationPayload(title, body));
	}
}
