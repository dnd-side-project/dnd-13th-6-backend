package com.runky.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth API", description = "Runky 인증/인가 API입니다.")
public interface AuthApiSpec {

	@Operation(
		summary = "카카오 OAuth 콜백",
		description = "카카오 Authorization Code를 교환하여 로그인 처리합니다. 신규 사용자는 signupToken 쿠키를, 기존 사용자는 AT/RT 쿠키를 발급합니다."
	)
	ResponseEntity<ApiResponse<AuthResponse>> kakaoCallback(
		@Schema(description = "카카오 OAuth Authorization Code", example = "SplxlOBeZQQYbYS6WxSbIA")
		@RequestParam("code") String code
	);

	@Operation(
		summary = "회원가입 완료",
		description = "signupToken(쿠키)과 추가 정보를 받아 최종 등록 후 AccessToken/RefreshToken을 발급합니다."
	)
	ApiResponse<Void> completeSignup(
		@Schema(description = "가입 티켓 토큰(쿠키)", example = "uuid-string")
		@CookieValue("signupToken") String signupToken,

		@Schema(description = "회원가입 추가 정보")
		@RequestBody AuthRequest.Signup request,
		HttpServletResponse servletResponse
	);

	@Operation(
		summary = "토큰 재발급",
		description = "RefreshToken(쿠키)을 사용해 AccessToken/RefreshToken을 재발급합니다."
	)
	ApiResponse<Void> refresh(
		@Schema(description = "리프레시 토큰(쿠키)", example = "jwt-refresh-token")
		@CookieValue("refreshToken") String refreshToken,

		HttpServletResponse servletResponse
	);

	@Operation(
		summary = "로그아웃",
		description = "서버 측 RT 기록을 삭제하고, AT/RT/ST 쿠키를 제거합니다."
	)
	ApiResponse<Void> logout(
		@Schema(description = "리프레시 토큰(쿠키)", example = "jwt-refresh-token")
		@CookieValue("refreshToken") String refreshToken,

		HttpServletResponse servletResponse
	);
}
