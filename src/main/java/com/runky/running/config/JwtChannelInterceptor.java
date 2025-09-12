package com.runky.running.config;

import static com.runky.running.api.RunningSocketConstants.*;
import static com.runky.running.config.CookieAuthHandshakeInterceptor.*;

import java.util.Map;
import java.util.Optional;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;
import com.runky.global.security.auth.MemberPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Security 메시지 인가보다 먼저
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

	private final TokenDecoder tokenDecoder;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null)
			return message;
		if (!StompCommand.CONNECT.equals(accessor.getCommand()))
			return message;

		Map<String, Object> attrs = accessor.getSessionAttributes();
		if (attrs != null) {
			Authentication pre = (Authentication)attrs.remove(ATTR_WS_AUTH);
			if (pre != null) {
				accessor.setUser(pre);
				return message;
			}
		}

		String jwtToken =
			Optional.ofNullable(accessor.getFirstNativeHeader(AUTHORIZATION))
				.filter(h -> h.startsWith(BEARER_))
				.map(h -> h.substring(BEARER_.length()))
				.filter(StringUtils::hasText)
				.map(String::trim)
				.orElseThrow(() -> new GlobalException(AuthErrorCode.INVALID_TOKEN));

		AccessTokenClaims claims = tokenDecoder.decodeAccess(jwtToken);
		MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal.memberId(), null,
			principal.authorities());

		accessor.setUser(authentication);
		attrs.put(ATTR_MEMBER_ID, claims.memberId());
		removeNativeHeader(accessor, AUTHORIZATION);
		return message;
	}

	private void removeNativeHeader(StompHeaderAccessor acc, String name) {
		if (acc.toNativeHeaderMap() != null)
			acc.toNativeHeaderMap().remove(name);
	}
}
