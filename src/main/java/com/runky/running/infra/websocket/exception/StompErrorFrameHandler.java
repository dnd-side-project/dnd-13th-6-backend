package com.runky.running.infra.websocket.exception;

import static org.springframework.util.MimeTypeUtils.*;

import java.nio.charset.StandardCharsets;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompConversionException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runky.global.response.ApiResponse;
import com.runky.running.error.RunningErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompErrorFrameHandler extends StompSubProtocolErrorHandler {

	private final ObjectMapper objectMapper;

	@Override
	public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable ex) {
		Throwable cause = rootCause(ex);

		RunningErrorCode code = mapToErrorCode(cause);
		String reason = resolveReason(cause, code);

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
		accessor.setLeaveMutable(true);
		accessor.setContentType(APPLICATION_JSON);
		accessor.setMessage(code.name());
		copyReceiptId(clientMessage, accessor);

		byte[] payload = toJson(ApiResponse.error(code, reason));
		return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
	}

	private void copyReceiptId(@Nullable Message<byte[]> clientMessage, StompHeaderAccessor errorHeaders) {
		if (clientMessage == null)
			return;
		StompHeaderAccessor clientHeaders = StompHeaderAccessor.wrap(clientMessage);
		String receiptId = clientHeaders.getReceipt();
		if (receiptId != null) {
			errorHeaders.setReceiptId(receiptId);
		}
	}

	private RunningErrorCode mapToErrorCode(Throwable t) {
		// 1) 인증/세션
		if (t instanceof AuthenticationException
			|| t instanceof IllegalStateException
			/*|| t instanceof ExpiredTokenException
			|| t instanceof InvalidTokenException*/) {
			return RunningErrorCode.UNAUTHORIZED_SESSION;
		}

		// 2) STOMP/JSON 변환 실패
		if (t instanceof StompConversionException
			|| t instanceof MessageConversionException
			|| t instanceof JsonProcessingException) {
			return RunningErrorCode.PAYLOAD_INVALID;
		}

		// 3) 라우팅/전달 실패
		if (t instanceof MessageDeliveryException) {
			return RunningErrorCode.EVENT_PUBLISH_FAILED;
		}

		// 4) 프로토콜 위반/필수 헤더 누락(일부 IllegalArgumentException)
		if (t instanceof IllegalArgumentException) {
			return RunningErrorCode.PAYLOAD_INVALID;
		}

		// 5) 그 외
		return RunningErrorCode.INTERNAL_ERROR;
	}

	private String resolveReason(Throwable t, RunningErrorCode code) {
		String msg = t.getMessage();
		if (msg == null || msg.isBlank()) {
			return code.getMessage();
		}
		// 메시지가 과하게 길면 잘라서 보냄
		if (msg.length() > 512) {
			return msg.substring(0, 512) + "…";
		}
		return msg;
	}

	private byte[] toJson(ApiResponse<?> body) {
		try {
			return objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);

		} catch (JsonProcessingException e) {
			// 직렬화 실패 시 텍스트로 폴백
			String fallback = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"serialization failed\"}";
			return fallback.getBytes(StandardCharsets.UTF_8);
		}
	}

	private Throwable rootCause(Throwable t) {
		Throwable cur = t;
		while (cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		return cur;
	}
}
