package com.runky.notification.infrastructure.push;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.runky.notification.domain.push.PushSender;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FirebaseCloudMessagingClient implements PushSender {

	private static final int MAX_FCM_BATCH = 500;

	private final FirebaseMessaging fcm;

	private static boolean isInvalidToken(FirebaseMessagingException e) {
		MessagingErrorCode code = e.getMessagingErrorCode();
		return code == MessagingErrorCode.UNREGISTERED
			|| code == MessagingErrorCode.INVALID_ARGUMENT
			|| code == MessagingErrorCode.SENDER_ID_MISMATCH;
	}

	private static String nvl(String s) {
		return s == null ? "" : s;
	}

	private static Map<String, String> ns(Map<String, String> m) {
		return m == null ? Map.of() : m;
	}

	@Override
	public SendResult sendUnicast(String token, NotificationPayload payload) {
		try {
			Message message = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
					.setTitle(nvl(payload.title()))
					.setBody(nvl(payload.body()))
					.build())
				.putAllData(ns(payload.data()))
				.build();

			fcm.send(message);
			return new SendResult(1, 0, List.of());

		} catch (FirebaseMessagingException e) {
			return isInvalidToken(e) ? new SendResult(0, 1, List.of(token))
				: new SendResult(0, 1, List.of());
		} catch (Exception e) {
			return new SendResult(0, 1, List.of());
		}
	}

	@Override
	public SendResult sendMulticast(List<String> tokens, NotificationPayload payload) {
		if (tokens == null || tokens.isEmpty())
			return new SendResult(0, 0, List.of());

		int success = 0;
		int failure = 0;
		List<String> invalids = new ArrayList<>();

		for (int i = 0; i < tokens.size(); i += MAX_FCM_BATCH) {
			List<String> chunk = tokens.subList(i, Math.min(i + MAX_FCM_BATCH, tokens.size()));
			try {
				MulticastMessage message = MulticastMessage.builder()
					.addAllTokens(chunk)
					.setNotification(Notification.builder()
						.setTitle(nvl(payload.title()))
						.setBody(nvl(payload.body()))
						.build())
					.putAllData(ns(payload.data()))
					.build();

				BatchResponse br = fcm.sendEachForMulticast(message);

				success += br.getSuccessCount();
				failure += br.getFailureCount();

				// 실패 응답 중 "무효 토큰"만 수집
				List<SendResponse> responses = br.getResponses();
				for (int idx = 0; idx < responses.size(); idx++) {
					SendResponse r = responses.get(idx);
					if (!r.isSuccessful() && r.getException() instanceof FirebaseMessagingException fme) {
						if (isInvalidToken(fme))
							invalids.add(chunk.get(idx));
					}
				}

			} catch (Exception e) {
				// 호출 자체 실패: 이 청크 전체 실패로 처리
				failure += chunk.size();
			}
		}

		return new SendResult(success, failure, Collections.unmodifiableList(invalids));
	}

}
