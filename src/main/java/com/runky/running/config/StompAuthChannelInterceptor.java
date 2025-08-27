package com.runky.running.config;

import static com.runky.running.config.JwtHandshakeInterceptor.*;

import java.security.Principal;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

		// CONNECT 프레임일 때 세션에 저장된 인증을 simpUser로 세팅
		if (StompCommand.CONNECT.equals(acc.getCommand())) {
			Map<String, Object> attrs = acc.getSessionAttributes();
			if (attrs != null) {
				Object auth = attrs.get(WS_AUTH_ATTR);
				if (auth instanceof Principal p) {
					acc.setUser(p);
				}
			}
		}
		return message;
	}
}
