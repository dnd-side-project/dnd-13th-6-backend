package com.runky.running.infra.websocket.outbound;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class OutboundChannelLogger implements ChannelInterceptor {

	private static final String LOG_FORMAT_CONNECT_ACK = "[OUT] [CONNECTED] sessionId=%s";
	private static final String LOG_FORMAT_MESSAGE = "[OUT] [MESSAGE] sessionId=%s subscriptionId=%s destination=%s";
	private static final String LOG_FORMAT_HEARTBEAT = "[OUT][HEARTBEAT] sessionId=%s";
	private static final String LOG_FORMAT_DISCONNECT_ACK = "[OUT] [DISCONNECT_ACK] sessionId=%s";
	private static final String LOG_FORMAT_DEFAULT = "[OUT] [OTHER] type=%s (stompCommand=%s) sessionId=%s";

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		SimpMessageHeaderAccessor simp = SimpMessageHeaderAccessor.wrap(message);
		StompHeaderAccessor stomp = StompHeaderAccessor.wrap(message);
		SimpMessageType messageType = simp.getMessageType();

		String sessionId = simp.getSessionId();
		String destination = simp.getDestination();
		String subscriptionId = simp.getSubscriptionId();

		switch (messageType) {
			case HEARTBEAT -> log.debug(String.format(LOG_FORMAT_HEARTBEAT, sessionId));

			case CONNECT_ACK -> log.info(String.format(LOG_FORMAT_CONNECT_ACK, sessionId));

			case MESSAGE -> log.info(String.format(LOG_FORMAT_MESSAGE, sessionId, subscriptionId, destination));

			case DISCONNECT_ACK -> log.info(String.format(LOG_FORMAT_DISCONNECT_ACK, sessionId));

			default -> {
				String command = stomp.getCommand().name();
				log.debug(String.format(LOG_FORMAT_DEFAULT, messageType, command, sessionId));
			}
		}
		return message;
	}

}
