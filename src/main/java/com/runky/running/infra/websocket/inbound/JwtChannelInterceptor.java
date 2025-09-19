package com.runky.running.infra.websocket.inbound;

import static com.runky.running.constants.RunningSocketConstants.*;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.runky.auth.domain.AuthService;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.running.infra.websocket.auth.StompAuthorizationHeaderTokenResolver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

	private final AuthService authService;
	private final StompAuthorizationHeaderTokenResolver tokenResolver;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
			return message;
		}

		Authentication authentication = resolveAuthentication(accessor);

		if (authentication != null) {
			accessor.setUser(authentication);
			storePrincipal(accessor, authentication);
		}

		removeAuthorizationHeader(accessor);
		return message;
	}

	private Authentication resolveAuthentication(StompHeaderAccessor accessor) {
		if (hasHandshakeAuthentication(accessor)) {
			return (Authentication)accessor.getSessionAttributes().get(AUTHENTICATION);
		}

		String jwt = tokenResolver.resolve(accessor);
		return authService.authenticate(jwt);
	}

	private boolean hasHandshakeAuthentication(StompHeaderAccessor accessor) {
		Map<String, Object> attrs = accessor.getSessionAttributes();
		return attrs != null && attrs.get(AUTHENTICATION) instanceof Authentication;
	}

	private void storePrincipal(StompHeaderAccessor accessor, Authentication auth) {
		MemberPrincipal principal = authService.principalOf(auth);
		if (principal != null) {
			Map<String, Object> attrs = accessor.getSessionAttributes();
			if (attrs != null) {
				attrs.put(MEMBER_PRINCIPAL, principal);
			}
		}
	}

	private void removeAuthorizationHeader(StompHeaderAccessor accessor) {
		accessor.toNativeHeaderMap().remove(AUTHORIZATION);
	}
}
