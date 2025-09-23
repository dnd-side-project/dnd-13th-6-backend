package com.runky.running.interfaces.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runky.global.response.ApiResponse;
import com.runky.running.error.RunningErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

	/**
	 * 언제 발생하나?
	 * - Inbound 바인딩/검증 단계에서 발생.
	 * - 클라이언트가 보낸 STOMP payload가 @Valid @Payload 대상 DTO로 바인딩된 뒤,
	 *   Bean Validation 제약(@NotNull, @DecimalMin/@Max, @PositiveOrZero 등)을 위반하면
	 *   Spring이 BindingResult를 만들어 MethodArgumentNotValidException으로 포장한다.
	 * 대표 사례
	 * - 좌표 범위 위반(x∉[-180,180], y∉[-90,90]), null 필드, 음수 timestamp 등
	 * 반환
	 * - INVALID_LOCATION_VALUE(R303)
	 */
	@MessageExceptionHandler(MethodArgumentNotValidException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMethodArgumentNotValid(MethodArgumentNotValidException ex, Message<?> message) {
		FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
		String reason = (fieldError == null)
			? RunningErrorCode.INVALID_LOCATION_VALUE.getMessage()
			: fieldError.getField() + ": " + fieldError.getDefaultMessage();
		log.debug("[WS][Validation] {}", reason, ex);
		return ApiResponse.error(RunningErrorCode.INVALID_LOCATION_VALUE, reason);
	}

	/**
	 * 언제 발생하나?
	 * - Inbound STOMP 프레임 → 메시지 변환 과정에서 발생.
	 * - STOMP 헤더/프레임을 목표 타입으로 변환하는 도중(예: 구독/전송 프레임 파싱 실패) 예외가 터진다.
	 * 대표 사례
	 * - 잘못된 STOMP 프레임 포맷, 헤더 누락/형식 오류
	 * 반환
	 * - PAYLOAD_INVALID(R304)
	 */
	@MessageExceptionHandler(org.springframework.messaging.simp.stomp.StompConversionException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onStompConversion(org.springframework.messaging.simp.stomp.StompConversionException ex,
		Message<?> msg) {
		log.debug("[WS][StompConversion] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.PAYLOAD_INVALID, RunningErrorCode.PAYLOAD_INVALID.getMessage());
	}

	/**
	 * 언제 발생하나?
	 * - Inbound/Outbound JSON 직렬화/역직렬화 및 메시지 변환 단계에서 발생.
	 * - Jackson(ObjectMapper) 또는 MessageConverter가 타입/포맷 불일치로 변환에 실패한 경우.
	 * 대표 사례
	 * - JSON 구문 오류, 필드 타입 불일치, 지원하지 않는 콘텐츠 타입
	 * 반환
	 * - PAYLOAD_INVALID(R304)
	 */
	@MessageExceptionHandler({MessageConversionException.class, JsonProcessingException.class})
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onOutboundConversion(Exception ex, Message<?> msg) {
		log.debug("[WS][OutboundConversion] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.PAYLOAD_INVALID, RunningErrorCode.PAYLOAD_INVALID.getMessage());
	}

	/**
	 * 언제 발생하나?
	 * - 세션/인증 맥락이 필요한 구간에서 인증 정보가 없거나 무효할 때.
	 * - Handshake/ChannelInterceptor/JWT 검증 실패, 세션 속성에서 Principal 누락 등.
	 * 대표 사례
	 * - 인증 토큰 만료/서명 오류, 세션 Attribute에 Principal 없음, SecurityContext 미설정
	 * 반환
	 * - UNAUTHORIZED_SESSION(R305)
	 */
	@MessageExceptionHandler({IllegalStateException.class, AuthenticationException.class})
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onUnauthorizedSession(Exception ex, Message<?> msg) {
		log.warn("[WS][UnauthorizedSession] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.UNAUTHORIZED_SESSION,
			RunningErrorCode.UNAUTHORIZED_SESSION.getMessage());
	}

	/**
	 * 언제 발생하나?
	 * - 메시지를 브로커/구독 대상에게 전달하는 과정에서 실패할 때.
	 * 대표 사례
	 * - 구독 대상 없음, 브로커 연결/라우팅 문제, 전송 타임아웃
	 * 반환
	 * - EVENT_PUBLISH_FAILED(R902)
	 */
	@MessageExceptionHandler(MessageDeliveryException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMessageDelivery(MessageDeliveryException ex, Message<?> msg) {
		log.warn("[WS][DeliveryFailed] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.EVENT_PUBLISH_FAILED,
			RunningErrorCode.EVENT_PUBLISH_FAILED.getMessage());
	}

	/**
	 * 언제 발생하나?
	 * - 메시징 파이프라인 전반에서 발생하는 일반적인 예외의 상위 타입.
	 * 대표 사례
	 * - 채널/브로커 다운, 세션 상태 비정상, 라우팅 실패 등 구체 핸들러에 매칭되지 않은 MessagingException
	 * 반환
	 * - INTERNAL_ERROR(R399)
	 */
	@MessageExceptionHandler(MessagingException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onMessaging(MessagingException ex, Message<?> msg) {
		log.warn("[WS][Messaging] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.INTERNAL_ERROR, RunningErrorCode.INTERNAL_ERROR.getMessage());
	}

	@MessageExceptionHandler(Throwable.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ApiResponse<Void> onAny(Throwable ex, Message<?> message) {
		log.error("[WS][Unhandled] {}", ex.getMessage(), ex);
		return ApiResponse.error(RunningErrorCode.INTERNAL_ERROR, RunningErrorCode.INTERNAL_ERROR.getMessage());
	}
}
