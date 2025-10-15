package com.runky.auth.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.runky.auth.application.AuthCriteria;
import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "Auth API", description = "Runky 인증/인가 API입니다.")
public interface AuthApiSpec {
	@Operation(
		summary = "카카오 OAuth 콜백",
		description = """
			카카오 Authorization Code를 받아 로그인 플로우를 수행합니다.
			- 신규 사용자: SignupToken 발급 후 리다이렉트 응답
			- 기존 사용자: AuthExchangeToken 발급 후 리다이렉트 응답
			컨트롤러에서 리다이렉트를 직접 수행하므로 HTTP 바디는 없습니다.
			"""
	)
	void kakaoCallback(
		@Schema(description = "카카오 OAuth Authorization Code", example = "SplxlOBeZQQYbYS6WxSbIA")
		@RequestParam("code") String code,
		HttpServletResponse servletResponse
	);

	@Operation(
		summary = "Localhost용 카카오 OAuth 콜백",
		description = """
			카카오 Authorization Code를 받아 로그인 플로우를 수행합니다.
			- 신규 사용자: SignupToken 발급 후 리다이렉트 응답
			- 기존 사용자: AuthExchangeToken 발급 후 리다이렉트 응답
			컨트롤러에서 리다이렉트를 직접 수행하므로 HTTP 바디는 없습니다.
			"""
	)
	void devKakaoCallback(
		@Schema(description = "Localhost용 카카오 OAuth Authorization Code", example = "SplxlOBeZQQYbYS6WxSbIA")
		@RequestParam("code") String code,
		HttpServletResponse servletResponse
	);

	@Operation(
		summary = "회원가입 완료",
		description = """
			SignupToken(쿼리파라미터)과 추가 정보를 받아 최종 등록을 완료합니다.
			완료 후 AuthExchangeToken을 포함한 응답(리다이렉트 혹은 JSON)을 반환합니다.
			컨트롤러 구현에 따라 ResponseEntity<ApiResponse<...>> 형식으로 반환됩니다.
			"""
	)
	ResponseEntity<ApiResponse<SignupResponseHandler.SignupCompleteResponse>> completeSignup(
		@Schema(description = "가입 토큰(쿼리파라미터)", example = "uuid-string")
		@RequestParam("signupToken") String signupToken,

		@Schema(description = "회원가입 추가 정보(예: 닉네임)")
		@Valid @RequestBody AuthCriteria.AdditionalSignUpData request
	);

	@Operation(
		summary = "JWT 교환 (AuthExchangeToken → JWT)",
		description = """
			AuthExchangeToken을 제출하면 서버가 AccessToken/RefreshToken을 발급합니다.
			발급된 토큰은 응답 헤더에 설정됩니다.
			- Authorization: Bearer {AccessToken}
			- X-Refresh-Token: {RefreshToken}
			HTTP 바디는 ApiResponse<Void>로 응답합니다.
			"""
	)
	ApiResponse<Void> exchangeAuthToken(
		@Schema(description = "AuthExchangeToken 교환 요청 바디")
		@Valid @RequestBody AuthController.TokenExchangeRequest request,

		HttpServletResponse servletResponse
	);

	@Operation(
		summary = "JWT 갱신 (Refresh Token Rotation)",
		description = """
			X-Refresh-Token 요청 헤더의 RefreshToken으로 새 AccessToken/RefreshToken을 발급합니다.
			발급된 토큰은 응답 헤더에 설정됩니다.
			- Authorization: Bearer {AccessToken}
			- X-Refresh-Token: {RefreshToken}
			HTTP 바디는 ApiResponse<Void>로 응답합니다.
			"""
	)
	ApiResponse<Void> refreshToken(
		@Schema(description = "리프레시 토큰(요청 헤더)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
		@RequestHeader("X-Refresh-Token") String refreshToken,

		HttpServletResponse servletResponse
	);

	@Operation(
		summary = "로그아웃",
		description = """
			X-Refresh-Token 요청 헤더를 기반으로 서버측 RT 기록을 삭제합니다.
			클라이언트는 보유 중인 토큰을 폐기해야 합니다.
			HTTP 바디는 ApiResponse<Void>로 응답합니다.
			"""
	)
	ApiResponse<Void> logout(
		@Schema(description = "리프레시 토큰(요청 헤더)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
		@RequestHeader("X-Refresh-Token") String refreshToken
	);
}
