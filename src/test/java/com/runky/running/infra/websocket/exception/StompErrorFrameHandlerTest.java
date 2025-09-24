package com.runky.running.infra.websocket.exception;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompConversionException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runky.running.error.RunningErrorCode;

class StompErrorFrameHandlerTest {

	private final ObjectMapper om = new ObjectMapper();
	private final StompErrorFrameHandler handler = new StompErrorFrameHandler(om);

	private static Message<byte[]> dummyClientMessage() {
		StompHeaderAccessor acc = StompHeaderAccessor.create(StompCommand.SEND);
		acc.setSessionId("sess-1");
		return MessageBuilder.createMessage("x".getBytes(StandardCharsets.UTF_8), acc.getMessageHeaders());
	}

	private static Stream<Arguments> cases() {
		return Stream.of(
			Arguments.of(new AuthenticationCredentialsNotFoundException("no token"),
				RunningErrorCode.UNAUTHORIZED_SESSION),

			Arguments.of(new StompConversionException("bad stomp"),
				RunningErrorCode.PAYLOAD_INVALID),

			Arguments.of(new MessageConversionException("bad json"),
				RunningErrorCode.PAYLOAD_INVALID),

			Arguments.of(new JsonProcessingException("bad json") {
						 },
				RunningErrorCode.PAYLOAD_INVALID),

			Arguments.of(new MessageDeliveryException("no subscribers"),
				RunningErrorCode.EVENT_PUBLISH_FAILED),

			Arguments.of(new IllegalArgumentException("bad header"),
				RunningErrorCode.PAYLOAD_INVALID),

			Arguments.of(new RuntimeException("boom"),
				RunningErrorCode.INTERNAL_ERROR)
		);
	}

	@ParameterizedTest(name = "{index} ⇒ {0} → {1}")
	@MethodSource("cases")
	@DisplayName("컨트롤러 이전 파이프라인에서 발생한 예외는 STOMP ERROR 헤더 + ApiResponse.code로 표준화된다")
	void 예외가_ERROR프레임으로_매핑되고_code만_검증한다(Throwable ex, RunningErrorCode expected) throws Exception {
		// when
		Message<byte[]> errorMsg = handler.handleClientMessageProcessingError(dummyClientMessage(), ex);

		// then 1) 헤더: STOMP ERROR 프레임인지
		StompHeaderAccessor h = StompHeaderAccessor.wrap(errorMsg);
		assertThat(h.getCommand()).isEqualTo(StompCommand.ERROR);

		// then 2) 바디: JsonNode로 code만 검증
		JsonNode node = om.readTree(errorMsg.getPayload());
		assertThat(node.get("code").asText()).isEqualTo(expected.getCode());
	}
}
