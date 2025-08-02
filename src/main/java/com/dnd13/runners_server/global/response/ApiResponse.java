package com.dnd13.runners_server.global.response;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.dnd13.runners_server.global.error.ErrorCode;
import com.dnd13.runners_server.global.error.GlobalErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
@Getter
// @JsonPropertyOrder : json serialization 순서를 정의
@JsonPropertyOrder({"status", "code", "message", "result"})
public class ApiResponse<T> {
	private final HttpStatus status;
	private final String message;
	private final String code;

	@JsonInclude(JsonInclude.Include.NON_NULL) // 결과값이 공백일 경우 json에 포함하지 않도록
	private final T result;

	@Override
	public String toString() {
		return "ApiResponse{"
			+ "status="
			+ status
			+ ", message='"
			+ message
			+ '\''
			+ ", code='"
			+ code
			+ '\''
			+ ", result="
			+ result
			+ '}';
	}

	/* ==== 정적 팩토리 ==== */

	public static <T> ApiResponse<T> ok() {
		return success(null);
	}

	public static <T> ApiResponse<T> success(T result) {
		var code = GlobalSuccessCode.SUCCESS;
		return new ApiResponse<>(code.getStatus(), code.getCode(), code.getMessage(), result);
	}

	public static <T> ResponseEntity<ApiResponse<T>> okResponseEntity(T result) {
		return ResponseEntity.ok(success(result));
	}

	public static ResponseEntity<ApiResponse<Void>> okResponseEntity() {
		return ResponseEntity.ok(ok());
	}

	public static ApiResponse<Void> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), null);
	}

	public static ApiResponse<Void> error(ErrorCode errorCode, String overrideMessage) {
		return new ApiResponse<>(errorCode.getStatus(), errorCode.getCode(), overrideMessage, null);
	}

	/* ==== 생성자 ==== */
	private ApiResponse(HttpStatus status, String code, String message, T result) {
		Objects.requireNonNull(status, "HttpStatus must not be null");
		Objects.requireNonNull(code, "Code must not be null");
		Objects.requireNonNull(message, "Message must not be null");

		this.status = status;
		this.code = code;
		this.message = message;
		this.result = result;
	}
}

