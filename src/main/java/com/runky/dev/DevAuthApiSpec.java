package com.runky.dev;// package com.runky.dev;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.runky.auth.api.AuthRequest;
import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dev Auth API", description = "관리자 Auth API 입니다.")
public interface DevAuthApiSpec {

	@Operation(
		summary = "카카오 로그인 콜백 (DEV)",
		description = """
			카카오 OAuth2 콜백(code)을 받아 로그인 흐름을 완료합니다.
			- NEW_USER: signupToken 부여 및 온보딩 페이지로 리다이렉트
			- EXISTING_USER: AT/RT 발급 후 메인 페이지로 리다이렉트
			""",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "카카오에서 전달된 인증 코드",
			content = @Content(schema = @Schema(implementation = Void.class))
		)
	)
	ResponseEntity<ApiResponse<DevAuthResponse>> devKakaoCallback(
		@Parameter(description = "카카오 인증 코드", example = "auth_code_from_kakao") String code
	);

	@Operation(
		summary = "회원가입 완료 (DEV)",
		description = "온보딩 추가정보를 수집하여 가입을 완료하고, AT/RT를 응답 헤더로 반환합니다."
	)
	ResponseEntity<ApiResponse<Void>> devCompleteSignup(
		@Parameter(description = "사전 발급된 가입 토큰", example = "signup-token-xxx")
		@RequestHeader("X-Signup-Token") String signupToken,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "회원가입 추가 정보",
			content = @Content(schema = @Schema(implementation = AuthRequest.Signup.class))
		)
		@RequestBody AuthRequest.Signup request
	);

	@Operation(
		summary = "리프레시로 재발급 (DEV)",
		description = "Refresh Token을 이용해 Access/Refresh Token을 재발급하고, 둘 다 응답 헤더로 반환합니다."
	)
	ResponseEntity<ApiResponse<Void>> devRefresh(
		@Parameter(description = "리프레시 토큰", example = "refresh-token-xxx")
		@RequestHeader("X-Refresh-Token") String refreshToken
	);

	@Operation(
		summary = "로그아웃 (DEV)",
		description = "Refresh Token 기반 로그아웃 처리. (DEV: 쿠키 제거 없이 헤더만 사용)"
	)
	ResponseEntity<ApiResponse<Void>> devLogout(
		@Parameter(description = "리프레시 토큰", example = "refresh-token-xxx")
		@RequestHeader("X-Refresh-Token") String refreshToken
	);
}
