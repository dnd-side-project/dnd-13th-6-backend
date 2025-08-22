package com.runky.notification.infrastructure.push;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.runky.notification.domain.push.PushSender;

@Service
@Profile("!prod")
public class MockPushSender implements PushSender {

	@Override
	public SendResult sendUnicast(String token, NotificationPayload payload) {
		return new SendResult(1, 0, List.of());
	}

	@Override
	public SendResult sendMulticast(List<String> tokens, NotificationPayload payload) {
		return new SendResult(tokens == null ? 0 : tokens.size(), 0, List.of());
	}
}
