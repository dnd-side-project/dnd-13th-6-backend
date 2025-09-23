package com.runky.running.interfaces.websocket;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runky.global.response.ApiResponse;
import com.runky.running.error.RunningErrorCode;

class WebSocketExceptionHandlerTest {

	private final WebSocketExceptionHandler webSocketExceptionHandler = new WebSocketExceptionHandler();

	private static MethodParameter locationMessageMethodParameter() {
		try {
			Method publishMethod = RunningLocationWsController.class.getMethod(
				"publish",
				Long.class,
				RunningLocationWsController.LocationMessage.class,
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
			assertError(apiResponse, RunningErrorCode.INVALID_LOCATION_VALUE, expectedField + ":");
		}

		@Test
		void FieldError가_없으면_기본메시지() {
			MethodArgumentNotValidException exception = newMethodArgumentNotValidException(List.of());
			ApiResponse<Void> apiResponse = webSocketExceptionHandler.onMethodArgumentNotValid(exception, anyMessage());
			assertError(apiResponse, RunningErrorCode.INVALID_LOCATION_VALUE,
				RunningErrorCode.INVALID_LOCATION_VALUE.getMessage());
		}

		@ParameterizedTest(name = "x 최솟값 미만: x={0} ⇒ 허용은 -180 이상(포함)")
		@CsvSource({"-180.0001", "-1000"})
		void x_최솟값_미만이면_INVALID로_매핑(double ignored) {
			MethodArgumentNotValidException ex = newMethodArgumentNotValidException(
				List.of(fieldError("x", "허용 범위는 -180 이상입니다")));
			ApiResponse<Void> res = webSocketExceptionHandler.onMethodArgumentNotValid(ex, anyMessage());
			assertError(res, RunningErrorCode.INVALID_LOCATION_VALUE, "x", "-180");
		}

		@ParameterizedTest(name = "x 최댓값 초과: x={0} ⇒ 허용은 180 이하(포함)")
		@CsvSource({"180.0001", "1000"})
		void x_최댓값_초과이면_INVALID로_매핑(double ignored) {
			MethodArgumentNotValidException ex = newMethodArgumentNotValidException(
				List.of(fieldError("x", "허용 범위는 180 이하입니다")));
			ApiResponse<Void> res = webSocketExceptionHandler.onMethodArgumentNotValid(ex, anyMessage());
			assertError(res, RunningErrorCode.INVALID_LOCATION_VALUE, "x", "180");
		}

		@ParameterizedTest(name = "y 최솟값 미만: y={0} ⇒ 허용은 -90 이상(포함)")
		@CsvSource({"-90.0001", "-1000"})
		void y_최솟값_미만이면_INVALID로_매핑(double ignored) {
			MethodArgumentNotValidException ex = newMethodArgumentNotValidException(
				List.of(fieldError("y", "허용 범위는 -90 이상입니다")));
			ApiResponse<Void> res = webSocketExceptionHandler.onMethodArgumentNotValid(ex, anyMessage());
			assertError(res, RunningErrorCode.INVALID_LOCATION_VALUE, "y", "-90");
		}

		@ParameterizedTest(name = "y 최댓값 초과: y={0} ⇒ 허용은 90 이하(포함)")
		@CsvSource({"90.0001", "1000"})
		void y_최댓값_초과이면_INVALID로_매핑(double ignored) {
			MethodArgumentNotValidException ex = newMethodArgumentNotValidException(
				List.of(fieldError("y", "허용 범위는 90 이하입니다")));
			ApiResponse<Void> res = webSocketExceptionHandler.onMethodArgumentNotValid(ex, anyMessage());
			assertError(res, RunningErrorCode.INVALID_LOCATION_VALUE, "y", "90");
		}

		@ParameterizedTest(name = "timestamp 음수: ts={0} ⇒ 허용은 0 이상(포함)")
		@CsvSource({"-1", "-100"})
		void timestamp_음수이면_INVALID로_매핑(long ignored) {
			MethodArgumentNotValidException ex = newMethodArgumentNotValidException(
				List.of(fieldError("timestamp", "0 이상이어야 합니다")));
			ApiResponse<Void> res = webSocketExceptionHandler.onMethodArgumentNotValid(ex, anyMessage());
			assertError(res, RunningErrorCode.INVALID_LOCATION_VALUE, "timestamp", "0");
		}
	}

	@Nested
	class 메시징_파이프라인_핸들러들 {

		@Test
		void StompConversion_은_PAYLOAD_INVALID로_매핑() {
			org.springframework.messaging.simp.stomp.StompConversionException exception =
				new org.springframework.messaging.simp.stomp.StompConversionException("bad stomp");
			ApiResponse<Void> apiResponse =
				webSocketExceptionHandler.onStompConversion(exception, anyMessage());
			assertError(apiResponse, RunningErrorCode.PAYLOAD_INVALID,
				RunningErrorCode.PAYLOAD_INVALID.getMessage());
		}

		@ParameterizedTest(name = "Outbound 변환 예외 메시지: \"{0}\" → PAYLOAD_INVALID")
		@ValueSource(strings = {"invalid json", "unknown type", ""})
		void OutboundConversion_은_PAYLOAD_INVALID로_매핑(String exceptionMessage) {
			ApiResponse<Void> responseFromMessageConversion =
				webSocketExceptionHandler.onOutboundConversion(new MessageConversionException(exceptionMessage),
					anyMessage());
			ApiResponse<Void> responseFromJsonProcessing =
				webSocketExceptionHandler.onOutboundConversion(new JsonProcessingException(exceptionMessage) {
				}, anyMessage());
			assertError(responseFromMessageConversion, RunningErrorCode.PAYLOAD_INVALID,
				RunningErrorCode.PAYLOAD_INVALID.getMessage());
			assertError(responseFromJsonProcessing, RunningErrorCode.PAYLOAD_INVALID,
				RunningErrorCode.PAYLOAD_INVALID.getMessage());
		}

		@NullAndEmptySource
		@ValueSource(strings = {"broker down", "no session"})
		void MessagingException_은_INTERNAL_ERROR로_매핑(String exceptionMessage) {
			MessagingException exception = new MessagingException(exceptionMessage);
			ApiResponse<Void> apiResponse =
				webSocketExceptionHandler.onMessaging(exception, anyMessage());
			assertError(apiResponse, RunningErrorCode.INTERNAL_ERROR, RunningErrorCode.INTERNAL_ERROR.getMessage());
		}

		@Test
		void Throwable_Fallback_도_INTERNAL_ERROR로_매핑() {
			ApiResponse<Void> apiResponse =
				webSocketExceptionHandler.onAny(new RuntimeException("boom"), anyMessage());
			assertError(apiResponse, RunningErrorCode.INTERNAL_ERROR, RunningErrorCode.INTERNAL_ERROR.getMessage());
		}
	}
}
