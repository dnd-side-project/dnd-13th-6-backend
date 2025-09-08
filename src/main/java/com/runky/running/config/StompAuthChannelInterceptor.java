package com.runky.running.config;

import static com.runky.running.config.JwtHandshakeInterceptor.*;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.TokenRequiredException;
import com.runky.global.security.auth.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
	private static final Pattern RUNNING_ID_PATTERN = Pattern.compile("/topic/runnings/(\\d+)$");

	private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

	private static final String HDR_AUTHORIZATION = "Authorization";
	private static final String HDR_X_ACCESS_TOKEN = "X-Access-Token";
	private static final String BEARER_PREFIX = "Bearer ";

	private final TokenDecoder tokenDecoder;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

		// CONNECT
		if (StompCommand.CONNECT.equals(acc.getCommand())) {
			String token = resolveFromStompHeaders(acc);
			if (StringUtils.hasText(token)) {
				acc.setUser(buildAuthentication(token));
				removeHeader(acc, HDR_AUTHORIZATION);
				removeHeader(acc, HDR_X_ACCESS_TOKEN);
				log.info("[WS][CONNECT] sessionId={}, memberId={}", acc.getSessionId(), resolveMemberId(acc.getUser()));
			} else {
				Map<String, Object> attrs = acc.getSessionAttributes();
				if (attrs != null) {
					Object auth = attrs.get(WS_AUTH_ATTR);
					if (auth instanceof Principal p) {
						acc.setUser(p);
					}
				}
				if (acc.getUser() == null) {
					throw new MessagingException("Missing authentication for STOMP CONNECT");
				}
				log.info("[WS][CONNECT][FALLBACK] sessionId={}, memberId={}", acc.getSessionId(),
					resolveMemberId(acc.getUser()));
			}
		}

		// 인증 강제
		if ((StompCommand.SUBSCRIBE.equals(acc.getCommand()) || StompCommand.SEND.equals(acc.getCommand()))
			&& acc.getUser() == null) {
			throw new MessagingException("Unauthenticated STOMP session");
		}

		// SUBSCRIBE 로깅
		if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
			String dest = acc.getDestination();      // 예: /topic/runnings/42
			Long runningId = extractRunningId(dest);
			Long memberId = resolveMemberId(acc.getUser());
			log.info("[WS][SUBSCRIBE] dest={}, runningId={}, memberId={}, sessionId={}, subscriptionId={}",
				dest, runningId, memberId, acc.getSessionId(), acc.getSubscriptionId());
		}

		// SEND 로깅 (클라이언트 → 서버, 컨트롤러 진입 전의 원문 바디)
		if (StompCommand.SEND.equals(acc.getCommand())) {
			String dest = acc.getDestination();      // 예: /app/runnings/42/location
			Long memberId = resolveMemberId(acc.getUser());
			String body = payloadAsString(message.getPayload());
			log.info("[WS][SEND] dest={}, memberId={}, sessionId={}, body={}",
				dest, memberId, acc.getSessionId(), abbreviate(body));
		}

		return message;
	}

	// --- helpers ---

	private Authentication buildAuthentication(String token) {
		try {
			AccessTokenClaims claims = tokenDecoder.decodeAccess(token);
			MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());
			return new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
		} catch (ExpiredTokenException e) {
			throw new MessagingException("Expired token", e);
		} catch (TokenRequiredException e) {
			throw new MessagingException("Invalid token", e);
		}
	}

	private String resolveFromStompHeaders(StompHeaderAccessor acc) {
		String authz = acc.getFirstNativeHeader(HDR_AUTHORIZATION);
		if (StringUtils.hasText(authz) && authz.startsWith(BEARER_PREFIX)) {
			return authz.substring(BEARER_PREFIX.length()).trim();
		}
		String x = acc.getFirstNativeHeader(HDR_X_ACCESS_TOKEN);
		return StringUtils.hasText(x) ? x.trim() : null;
	}

	private void removeHeader(StompHeaderAccessor acc, String name) {
		if (acc.toNativeHeaderMap() != null) {
			acc.toNativeHeaderMap().remove(name);
		}
	}

	private Long resolveMemberId(Principal principal) {
		if (principal instanceof Authentication auth && auth.getPrincipal() instanceof MemberPrincipal mp)
			return mp.memberId();
		return null;
	}

	private Long extractRunningId(String destination) {
		if (destination == null) {
			return null;
		}
		Matcher matcher = RUNNING_ID_PATTERN.matcher(destination);

		return matcher.find() ? Long.valueOf(matcher.group(1)) : null;
	}

	private String payloadAsString(Object payload) {
		if (payload instanceof byte[] b)
			return new String(b, StandardCharsets.UTF_8);
		return String.valueOf(payload);
	}

	private String abbreviate(String s) {
		if (s == null)
			return null;
		int limit = 1000;
		return s.length() > limit ? s.substring(0, limit) + "...(truncated)" : s;
	}
}
