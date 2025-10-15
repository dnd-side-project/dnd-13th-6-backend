package com.runky.auth.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.runky.auth.application.AuthResult;
import com.runky.global.response.ApiResponse;

/**
 * 회원가입 응답 처리 전담
 */
@Component
public class SignupResponseHandler {

	/**
	 * 회원가입 완료 응답
	 * - AuthExchangeToken을 JSON Body로 반환
	 */
	public ResponseEntity<ApiResponse<SignupCompleteResponse>> handle(AuthResult.SignupResponseAction action) {

		if (action instanceof AuthResult.SignupResponseAction.SignupCompleteResponse complete) {
			return handleSignupComplete(complete.authExchangeToken());
		}
		throw new IllegalArgumentException("Unknown action type");

	}

	private ResponseEntity<ApiResponse<SignupCompleteResponse>> handleSignupComplete(String authExchangeToken) {

		SignupCompleteResponse dto = new SignupCompleteResponse(
			authExchangeToken,
			"Signup completed successfully"
		);

		return ResponseEntity.ok(ApiResponse.success(dto));
	}

	/**
	 * 회원가입 완료 응답 DTO
	 */
	public record SignupCompleteResponse(
		String authCode,
		String message
	) {
	}
}
