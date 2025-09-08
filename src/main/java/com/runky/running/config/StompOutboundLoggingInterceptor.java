package com.runky.running.config;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompOutboundLoggingInterceptor implements ChannelInterceptor {

	private static final Logger log = LoggerFactory.getLogger(StompOutboundLoggingInterceptor.class);

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

		// 브로커 → 클라이언트로 나가는 실제 메시지
		if (StompCommand.MESSAGE.equals(acc.getCommand())
			|| SimpMessageType.MESSAGE.equals(acc.getMessageType())) {
			String dest = acc.getDestination(); // 예: /topic/runnings/42
			String sessionId = acc.getSessionId();
			String subId = acc.getSubscriptionId();

			String body = (message.getPayload() instanceof byte[] b)
				? new String(b, StandardCharsets.UTF_8)
				: String.valueOf(message.getPayload());

			log.info("[WS][OUTBOUND][MESSAGE] dest={}, sessionId={}, subscriptionId={}, body={}",
				dest, sessionId, subId, body.length() > 1000 ? body.substring(0, 1000) + "...(truncated)" : body);
		}
		return message;
	}
}
