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
import com.runky.running.infra.websocket.session.WebSocketSessionManager;

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
	private final WebSocketSessionManager sessionManager;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		StompCommand command = accessor.getCommand();

		if (StompCommand.CONNECT.equals(command)) {
			handleConnect(accessor);
		} else if (StompCommand.DISCONNECT.equals(command)) {
			handleDisconnect(accessor);
		}

		return message;
	}

	private void handleConnect(StompHeaderAccessor accessor) {
		Authentication authentication = resolveAuthentication(accessor);

		if (authentication != null) {
			accessor.setUser(authentication);
			MemberPrincipal principal = storePrincipal(accessor, authentication);

			// 세션 등록 (재연결 시 이전 세션 자동 만료)
			if (principal != null && accessor.getSessionId() != null) {
				sessionManager.registerSession(principal.memberId(), accessor.getSessionId());
			}
		}

		removeAuthorizationHeader(accessor);
	}

	private void handleDisconnect(StompHeaderAccessor accessor) {
		String sessionId = accessor.getSessionId();
		if (sessionId != null) {
			sessionManager.unregisterSession(sessionId);
		}
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

	private MemberPrincipal storePrincipal(StompHeaderAccessor accessor, Authentication auth) {
		MemberPrincipal principal = authService.principalOf(auth);
		if (accessor.getSessionAttributes() != null) {
			accessor.getSessionAttributes().put(MEMBER_PRINCIPAL, principal);
		}
		return principal;
	}

	private void removeAuthorizationHeader(StompHeaderAccessor accessor) {
		accessor.toNativeHeaderMap().remove(AUTHORIZATION);
	}
}
