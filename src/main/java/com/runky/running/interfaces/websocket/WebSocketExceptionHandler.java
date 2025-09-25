package com.runky.running.interfaces.websocket;

import java.security.Principal;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runky.global.response.ApiResponse;
import com.runky.running.error.RunningErrorCode;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

	// DTO 바인딩/검증 예외

	/**
	 * @Valid @Payload DTO 검증 실패
	 * ex) 필수 값 누락, 범위 위반 등
	 * --> INVALID_INPUT(R300)
	 */
	@MessageExceptionHandler(MethodArgumentNotValidException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMethodArgumentNotValid(MethodArgumentNotValidException ex, Message<?> message) {
		FieldError fe = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
		String reason = (fe == null) ? RunningErrorCode.INVALID_INPUT.getMessage()
			: fe.getField() + ": " + fe.getDefaultMessage();
		log.debug("[WS][Validation] {}", reason, ex);
		return ApiResponse.error(RunningErrorCode.INVALID_INPUT, reason);
	}

	/**
	 * 메서드 파라미터 Bean Validation 제약 위반(@Validated + @Min 등)
	 * --> CONSTRAINT_VIOLATION(R310)
	 */
	@MessageExceptionHandler(ConstraintViolationException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onConstraintViolation(ConstraintViolationException ex, Message<?> msg) {
		String reason = ex.getConstraintViolations().stream().findFirst()
			.map(v -> v.getPropertyPath() + ": " + v.getMessage())
			.orElse(RunningErrorCode.CONSTRAINT_VIOLATION.getMessage());
		log.debug("[WS][ConstraintViolation] {}", reason, ex);
		return ApiResponse.error(RunningErrorCode.CONSTRAINT_VIOLATION, reason);
	}

	/**
	 * @Payload 역직렬화/직렬화 실패(JSON ↔ DTO)
	 * ex) 타입 불일치, enum 매핑 실패, JSON 구문 오류 등
	 * --> PAYLOAD_INVALID(R304)
	 */
	@MessageExceptionHandler(MessageConversionException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onPayloadConversion(MessageConversionException ex, Message<?> msg) {
		log.debug("[WS][PayloadConversion] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.PAYLOAD_INVALID, RunningErrorCode.PAYLOAD_INVALID.getMessage());
	}

	/**
	 * Outbound 응답 직렬화(Jackson) 실패
	 * ex) 반환 객체에 순환참조, 직렬화 불가 타입 포함 등
	 * --> OUTBOUND_SERIALIZATION_ERROR(R311)
	 */
	@MessageExceptionHandler(JsonProcessingException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onOutboundSerialization(JsonProcessingException ex, Message<?> msg) {
		log.debug("[WS][OutboundSerialize] {}", ex.getOriginalMessage(), ex);
		return ApiResponse.error(RunningErrorCode.OUTBOUND_SERIALIZATION_ERROR,
			RunningErrorCode.OUTBOUND_SERIALIZATION_ERROR.getMessage());
	}

	/**
	 * @Header/@DestinationVariable 타입 변환 실패
	 * ex) "abc" → Long 변환 시도
	 * --> TYPE_CONVERSION_FAILED(R308)
	 */
	@MessageExceptionHandler(ConversionFailedException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onTypeConversion(ConversionFailedException ex, Message<?> msg) {
		String reason = "타입 변환 실패: value=" + String.valueOf(ex.getValue())
			+ ", target=" + ex.getTargetType();
		log.debug("[WS][TypeConversion] {}", reason, ex);
		return ApiResponse.error(RunningErrorCode.TYPE_CONVERSION_FAILED, reason);
	}

	// 컨트롤러 내부에서 발생한 인증/인가 예외

	/**
	 * 권한 부족(@PreAuthorize 등)
	 * --> FORBIDDEN_WS_ACCESS(R306)
	 */
	@MessageExceptionHandler(AccessDeniedException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onAccessDenied(AccessDeniedException ex, Principal principal, Message<?> msg) {
		log.info("[WS][AccessDenied] user={} {}", principal != null ? principal.getName() : "anonymous", ex.toString());
		return ApiResponse.error(RunningErrorCode.FORBIDDEN_WS_ACCESS,
			RunningErrorCode.FORBIDDEN_WS_ACCESS.getMessage());
	}

	/**
	 * 인증 정보 없음/무효(메서드 보안 시점)
	 * --> UNAUTHORIZED_SESSION(R305)
	 */
	@MessageExceptionHandler({
		AuthenticationCredentialsNotFoundException.class,
		AuthenticationException.class
	})
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onUnauthorizedSession(Exception ex, Message<?> msg) {
		log.warn("[WS][Unauthorized] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.UNAUTHORIZED_SESSION,
			RunningErrorCode.UNAUTHORIZED_SESSION.getMessage());
	}

	// 도메인/비즈니스 로직 예외

	/**
	 * 잘못된 인자(서비스/도메인 입력 오류)
	 * --> BUSINESS_INVALID_ARGUMENT(R321)
	 */
	@MessageExceptionHandler(IllegalArgumentException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onIllegalArgument(IllegalArgumentException ex, Message<?> msg) {
		log.info("[WS][Biz:IllegalArgument] {}", ex.toString(), ex);
		return ApiResponse.error(RunningErrorCode.BUSINESS_INVALID_ARGUMENT,
			ex.getMessage() != null ? ex.getMessage()
				: RunningErrorCode.BUSINESS_INVALID_ARGUMENT.getMessage());
	}

	/**
	 * 도메인 상태 위반
	 * --> BUSINESS_ILLEGAL_STATE(R322)
	 */
	@MessageExceptionHandler(IllegalStateException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onIllegalState(IllegalStateException ex, Message<?> msg) {
		log.info("[WS][Biz:IllegalState] {}", ex.toString(), ex);
		return ApiResponse.error(RunningErrorCode.BUSINESS_ILLEGAL_STATE,
			ex.getMessage() != null ? ex.getMessage()
				: RunningErrorCode.BUSINESS_ILLEGAL_STATE.getMessage());
	}

	// 메시지 실행/전송 예외

	/**
	 * @MessageMapping 실행 중 예외 래핑
	 * --> MESSAGE_HANDLING_ERROR(R312)
	 */
	@MessageExceptionHandler(MessageHandlingException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMessageHandling(MessageHandlingException ex, Message<?> msg) {
		String reason = ex.getCause() != null ? ex.getCause().getMessage()
			: RunningErrorCode.MESSAGE_HANDLING_ERROR.getMessage();
		log.warn("[WS][Handling] {}", reason, ex);
		return ApiResponse.error(RunningErrorCode.MESSAGE_HANDLING_ERROR, reason);
	}

	/**
	 * convertAndSend 중 전송 실패
	 * --> EVENT_PUBLISH_FAILED(R902)
	 */
	@MessageExceptionHandler(MessageDeliveryException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMessageDelivery(MessageDeliveryException ex, Message<?> msg) {
		log.warn("[WS][DeliveryFailed] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.EVENT_PUBLISH_FAILED,
			RunningErrorCode.EVENT_PUBLISH_FAILED.getMessage());
	}

	/**
	 * 메시징 전반의 일반 예외(위에서 매칭되지 않은 MessagingException)
	 * --> MESSAGING_ERROR(R313)
	 */
	@MessageExceptionHandler(MessagingException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMessaging(MessagingException ex, Message<?> msg) {
		log.warn("[WS][Messaging] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.MESSAGING_ERROR, RunningErrorCode.MESSAGING_ERROR.getMessage());
	}

	// 폴백

	@MessageExceptionHandler(Throwable.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onAny(Throwable ex, Message<?> message) {
		log.error("[WS][Unhandled] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.INTERNAL_ERROR, RunningErrorCode.INTERNAL_ERROR.getMessage());
	}

}
