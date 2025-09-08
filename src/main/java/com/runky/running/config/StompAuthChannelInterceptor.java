package com.runky.running.config;

import static com.runky.running.config.JwtHandshakeInterceptor.*;

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

	private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

	// JwtCookieAuthFilter와 동일한 헤더 명세
	private static final String HDR_AUTHORIZATION = "Authorization";
	private static final String HDR_X_ACCESS_TOKEN = "X-Access-Token";
	private static final String BEARER_PREFIX = "Bearer ";

	private final TokenDecoder tokenDecoder;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(acc.getCommand())) {
			// A) STOMP CONNECT 헤더에서 우선 시도
			String token = resolveFromStompHeaders(acc);

			if (StringUtils.hasText(token)) {
				acc.setUser(buildAuthentication(token));
				// 보안상 토큰 헤더 제거(옵션)
				removeHeader(acc, HDR_AUTHORIZATION);
				removeHeader(acc, HDR_X_ACCESS_TOKEN);
			} else {
				// B) 헤더에 없으면 핸드셰이크 폴백(쿠키 기반)
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
			}
		}

		// 인증 강제: SUBSCRIBE / SEND
		if ((StompCommand.SUBSCRIBE.equals(acc.getCommand()) || StompCommand.SEND.equals(acc.getCommand()))
			&& acc.getUser() == null) {
			throw new MessagingException("Unauthenticated STOMP session");
		}

		// ✅ 구독 로그: 누가(memberId) 어떤 방(runningId) 구독했는지
		if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
			String dest = acc.getDestination();      // 예: /topic/runnings/42
			String sessionId = acc.getSessionId();
			Long runningId = extractRunningId(dest);
			Long memberId = resolveMemberId(acc.getUser());

			log.info("[WS][SUBSCRIBE] dest={}, runningId={}, memberId={}, sessionId={}",
				dest, runningId, memberId, sessionId);
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
		// Authorization: Bearer <token> -> X-Access-Token: <token>
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
		if (principal == null)
			return null;

		// StompAuthChannelInterceptor에서 setUser(auth) 했으므로 보통 Authentication이 들어옵니다.
		if (principal instanceof Authentication auth) {
			Object p = auth.getPrincipal();
			if (p instanceof MemberPrincipal mp) {
				return mp.memberId();
			}
		}
		return null;
	}

	private Long extractRunningId(String destination) {
		if (destination == null)
			return null;
		// /topic/runnings/{runningId} 형식만 매칭
		Matcher m = Pattern.compile("^/topic/runnings/(\\d+)$").matcher(destination);
		return m.find() ? Long.valueOf(m.group(1)) : null;
	}
}
