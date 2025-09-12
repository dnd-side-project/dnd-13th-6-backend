// src/main/java/com/runky/running/config/StompOutboundMessageLogger.java
package com.runky.running.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class StompOutboundMessageLogger implements ChannelInterceptor {

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		var sha = SimpMessageHeaderAccessor.wrap(message);

		// 아웃바운드로 나가는 STOMP MESSAGE는 SIMP 타입이 MESSAGE & 구독 ID(subId)가 존재
		if (sha.getMessageType() != SimpMessageType.MESSAGE)
			return message;
		String subId = sha.getSubscriptionId();
		if (subId == null)
			return message; // 구독자가 아니면 아웃바운드 메시지가 아님

		String sessionId = sha.getSessionId();
		String dest = sha.getDestination();

		log.info("WS MESSAGE→SUB: sessionId={}, subId={}, dest={}", sessionId, subId, dest);
		return message;
	}
}
