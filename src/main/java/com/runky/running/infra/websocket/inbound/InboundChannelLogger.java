package com.runky.running.infra.websocket.inbound;

import static com.runky.running.constants.RunningSocketConstants.*;
import static java.lang.String.*;

import java.util.Optional;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.runky.global.security.auth.MemberPrincipal;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class InboundChannelLogger implements ChannelInterceptor {

	private static final String LOG_FORMAT_HEARTBEAT = "[IN][HEARTBEAT] sessionId=%s";
	private static final String LOG_FORMAT_CONNECT = "[IN] [CONNECT] sessionId=%s memberId=%s";
	private static final String LOG_FORMAT_SUBSCRIBE = "[IN] [SUBSCRIBE] sessionId=%s destination=%s subscriptionId=%s memberId=%s";
	private static final String LOG_FORMAT_SEND = "[IN] [SEND] sessionId=%s destination=%s memberId=%s";
	private static final String LOG_FORMAT_UNSUBSCRIBE = "[IN] [UNSUBSCRIBE] sessionId=%s subscriptionId=%s memberId=%s";
	private static final String LOG_FORMAT_DISCONNECT = "[IN] [DISCONNECT]  sessionId=%s memberId=%s";
	private static final String LOG_FORMAT_DEFAULT = "[IN] %s sessionId=%s memberId=%s";

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor stomp = StompHeaderAccessor.wrap(message);
		StompCommand command = stomp.getCommand();

		String sessionId = stomp.getSessionId();
		Long memberId = memberIdFromSession(stomp);
		String destination = stomp.getDestination();
		String subscriptionId = stomp.getSubscriptionId();

		if (stomp.isHeartbeat()) {
			log.debug(format(LOG_FORMAT_HEARTBEAT, sessionId));
			return message;
		}

		switch (command) {
			case CONNECT -> log.info(format(LOG_FORMAT_CONNECT, sessionId, memberId));

			case SUBSCRIBE -> log.info(format(LOG_FORMAT_SUBSCRIBE, sessionId, destination, subscriptionId, memberId));

			//  case SEND -> log.info(format(LOG_FORMAT_SEND, sessionId, destination, memberId));

			case UNSUBSCRIBE -> log.info(format(LOG_FORMAT_UNSUBSCRIBE, sessionId, subscriptionId, memberId));

			case DISCONNECT -> log.info(format(LOG_FORMAT_DISCONNECT, sessionId, memberId));

			//	default -> log.debug(format(LOG_FORMAT_DEFAULT, command, sessionId, memberId));
		}
		return message;
	}

	private Long memberIdFromSession(StompHeaderAccessor accessor) {
		return Optional.of(accessor.getSessionAttributes())
			.map(attributes -> attributes.get(MEMBER_PRINCIPAL))
			.filter(MemberPrincipal.class::isInstance)
			.map(MemberPrincipal.class::cast)
			.map(MemberPrincipal::memberId)
			.orElse(null);
	}
}
