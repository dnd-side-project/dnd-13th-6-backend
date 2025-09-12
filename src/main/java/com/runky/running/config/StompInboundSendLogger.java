// src/main/java/com/runky/running/config/StompInboundSendLogger.java
package com.runky.running.config;

import static com.runky.running.api.RunningSocketConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE) // 다른 처리 끝나고 마지막에 로깅
public class StompInboundSendLogger implements ChannelInterceptor {

	private static final int PREVIEW_MAX = 200;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		var sha = SimpMessageHeaderAccessor.wrap(message);

		if (sha.getMessageType() != SimpMessageType.MESSAGE)
			return message;
		if (sha.getSubscriptionId() != null)
			return message; // 구독자가 아님 → 인바운드가 아님

		String sessionId = sha.getSessionId();
		String dest = sha.getDestination();

		log.info("WS SEND: sessionId={}, dest={}, size={}B, preview=`{}` | {}",
			sessionId, dest, byteSize(message.getPayload()), preview(message.getPayload()), who(sha));
		return message;
	}

	private String who(SimpMessageHeaderAccessor sha) {
		Object memberId = sha.getSessionAttributes() != null ? sha.getSessionAttributes().get(ATTR_MEMBER_ID) : null;
		var user = sha.getUser();
		String roles = (user instanceof Authentication a)
			? a.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","))
			: "ANON";
		String username = (user instanceof Authentication a) ? a.getName() : "anonymous";
		return "userId=" + memberId + ", username=" + username + ", roles=" + roles;
	}

	/* ===== helpers ===== */

	private int byteSize(Object payload) {
		if (payload instanceof byte[] b)
			return b.length;
		return String.valueOf(payload).getBytes(StandardCharsets.UTF_8).length;
	}

	private String preview(Object payload) {
		String s = (payload instanceof byte[] b) ? new String(b, StandardCharsets.UTF_8) : String.valueOf(payload);
		if (s == null)
			return "null";
		s = s.replaceAll("\\s+", " ").trim();
		return s.length() > PREVIEW_MAX ? s.substring(0, PREVIEW_MAX) + "...(truncated)" : s;
	}

}
