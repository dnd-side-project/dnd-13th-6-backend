package com.runky.running.interfaces.websocket;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runky.global.response.ApiResponse;
import com.runky.running.error.RunningErrorCode;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

class WebSocketExceptionHandlerTest {

	private final WebSocketExceptionHandler webSocketExceptionHandler = new WebSocketExceptionHandler();

	private static MethodParameter locationMessageMethodParameter() {
		try {
			Method publishMethod = RunningLocationWsController.class.getMethod(
				"publish",
				Long.class,
				LocationMessage.class,
				SimpMessageHeaderAccessor.class
			);
			return new MethodParameter(publishMethod, 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Message<?> anyMessage() {
		return MessageBuilder.withPayload("payload").build();
	}

	private static void assertError(ApiResponse<?> apiResponse, RunningErrorCode expectedCode, String... contains) {
		assertThat(apiResponse).isNotNull();
		assertThat(apiResponse.getCode()).isEqualTo(expectedCode.getCode());
		for (String token : contains) {
			assertThat(apiResponse.getMessage()).contains(token);
		}
	}

	/* ---------------------------------------------------------------------- */
	@Nested
	class MethodArgumentNotValid_핸들러 {

		private static FieldError fieldError(String field, String message) {
			return new FieldError("target", field, message);
		}

		private static MethodArgumentNotValidException newMethodArgumentNotValidException(
			List<FieldError> fieldErrorList) {
			BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
			fieldErrorList.forEach(bindingResult::addError);
			return new MethodArgumentNotValidException(anyMessage(), locationMessageMethodParameter(), bindingResult);
		}

		static Stream<Arguments> fieldErrorCases() {
			return Stream.of(
				Arguments.of(List.of(fieldError("x", "x(경도)는 -180 ~ 180 사이값 입니다.")), "x"),
				Arguments.of(List.of(fieldError("y", "y(위도)는 -90 ~ 90 사이값 입니다.")), "y"),
				Arguments.of(List.of(fieldError("timestamp", "0보다 커야합니다.")), "timestamp"),
				Arguments.of(List.of(fieldError("x", "Invalid x"), fieldError("y", "Invalid y")), "x")
			);
		}

		@ParameterizedTest(name = "첫번째 FieldError 노출: 기대필드={1}")
		@MethodSource("fieldErrorCases")
		void 첫번째_FieldError만_메시지에_노출(List<FieldError> fieldErrors, String expectedField) {
			MethodArgumentNotValidException exception = newMethodArgumentNotValidException(fieldErrors);
			ApiResponse<Void> apiResponse = webSocketExceptionHandler.onMethodArgumentNotValid(exception, anyMessage());
			assertError(apiResponse, RunningErrorCode.INVALID_INPUT, expectedField + ":");
		}

		@Test
		void FieldError가_없으면_기본메시지() {
			MethodArgumentNotValidException exception = newMethodArgumentNotValidException(List.of());
			ApiResponse<Void> apiResponse = webSocketExceptionHandler.onMethodArgumentNotValid(exception, anyMessage());
			assertError(apiResponse, RunningErrorCode.INVALID_INPUT,
				RunningErrorCode.INVALID_INPUT.getMessage());
		}
	}

	/* ---------------------------------------------------------------------- */
	@Nested
	class 바인딩_변환_검증_기타 {

		@Test
		void MessageConversionException_은_PAYLOAD_INVALID로_매핑() {
			ApiResponse<Void> response =
				webSocketExceptionHandler.onPayloadConversion(new MessageConversionException("invalid json"),
					anyMessage());
			assertError(response, RunningErrorCode.PAYLOAD_INVALID,
				RunningErrorCode.PAYLOAD_INVALID.getMessage());
		}

		@Test
		void JsonProcessingException_은_OUTBOUND_SERIALIZATION_ERROR로_매핑() {
			ApiResponse<Void> response =
				webSocketExceptionHandler.onOutboundSerialization(new JsonProcessingException("boom") {
				}, anyMessage());
			assertError(response, RunningErrorCode.OUTBOUND_SERIALIZATION_ERROR,
				RunningErrorCode.OUTBOUND_SERIALIZATION_ERROR.getMessage());
		}

		@Test
		void ConversionFailedException_은_TYPE_CONVERSION_FAILED로_매핑() {
			ConversionFailedException ex = new ConversionFailedException(
				TypeDescriptor.valueOf(String.class),
				TypeDescriptor.valueOf(Long.class),
				"abc",
				new NumberFormatException("For input string: \"abc\"")
			);
			ApiResponse<Void> res = webSocketExceptionHandler.onTypeConversion(ex, anyMessage());
			assertError(res, RunningErrorCode.TYPE_CONVERSION_FAILED, "타입 변환 실패", "target=java.lang.Long");
		}

		@Test
		void ConstraintViolationException_은_CONSTRAINT_VIOLATION로_매핑() {
			ConstraintViolationException ex = new ConstraintViolationException("violate",
				Set.<ConstraintViolation<?>>of());
			ApiResponse<Void> res = webSocketExceptionHandler.onConstraintViolation(ex, anyMessage());
			assertError(res, RunningErrorCode.CONSTRAINT_VIOLATION,
				RunningErrorCode.CONSTRAINT_VIOLATION.getMessage());
		}
	}

	/* ---------------------------------------------------------------------- */
	@Nested
	class 인증_인가_핸들러들 {

		@Test
		void AccessDeniedException_은_FORBIDDEN_WS_ACCESS로_매핑() {
			ApiResponse<Void> res = webSocketExceptionHandler.onAccessDenied(
				new AccessDeniedException("forbidden"), null, anyMessage());
			assertError(res, RunningErrorCode.FORBIDDEN_WS_ACCESS,
				RunningErrorCode.FORBIDDEN_WS_ACCESS.getMessage());
		}

		@Test
		void AuthenticationCredentialsNotFound_은_UNAUTHORIZED_SESSION으로_매핑() {
			ApiResponse<Void> res = webSocketExceptionHandler.onUnauthorizedSession(
				new AuthenticationCredentialsNotFoundException("no auth"), anyMessage());
			assertError(res, RunningErrorCode.UNAUTHORIZED_SESSION,
				RunningErrorCode.UNAUTHORIZED_SESSION.getMessage());
		}
	}

	/* ---------------------------------------------------------------------- */
	@Nested
	class 메시징_실행_전송_핸들러들 {

		@Test
		void MessageHandlingException_은_MESSAGE_HANDLING_ERROR로_매핑() {
			MessageHandlingException ex = new MessageHandlingException(anyMessage(), "handling failed",
				new IllegalStateException("boom cause"));
			ApiResponse<Void> res = webSocketExceptionHandler.onMessageHandling(ex, anyMessage());
			assertError(res, RunningErrorCode.MESSAGE_HANDLING_ERROR, "boom cause");
		}

		@Test
		void MessageDeliveryException_은_EVENT_PUBLISH_FAILED로_매핑() {
			MessageDeliveryException ex = new MessageDeliveryException("no subscribers");
			ApiResponse<Void> res = webSocketExceptionHandler.onMessageDelivery(ex, anyMessage());
			assertError(res, RunningErrorCode.EVENT_PUBLISH_FAILED,
				RunningErrorCode.EVENT_PUBLISH_FAILED.getMessage());
		}

		@NullAndEmptySource
		@ValueSource(strings = {"broker down", "no session"})
		@ParameterizedTest(name = "MessagingException(\"{0}\") → MESSAGING_ERROR")
		void MessagingException_은_MESSAGING_ERROR로_매핑(String msg) {
			MessagingException ex = new MessagingException(msg);
			ApiResponse<Void> res = webSocketExceptionHandler.onMessaging(ex, anyMessage());
			assertError(res, RunningErrorCode.MESSAGING_ERROR,
				RunningErrorCode.MESSAGING_ERROR.getMessage());
		}
	}

	/* ---------------------------------------------------------------------- */
	@Nested
	class 폴백 {

		@Test
		void Throwable_Fallback_은_INTERNAL_ERROR로_매핑() {
			ApiResponse<Void> res = webSocketExceptionHandler.onAny(new RuntimeException("boom"), anyMessage());
			assertError(res, RunningErrorCode.INTERNAL_ERROR, RunningErrorCode.INTERNAL_ERROR.getMessage());
		}
	}
}
