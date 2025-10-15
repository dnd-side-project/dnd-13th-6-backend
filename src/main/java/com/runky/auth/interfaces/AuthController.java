package com.runky.auth.interfaces;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runky.auth.application.AuthCriteria;
import com.runky.auth.application.AuthFacade;
import com.runky.auth.application.AuthResult;
import com.runky.auth.domain.AuthInfo;
import com.runky.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthFacade authFacade;
	private final OAuthResponseHandler oauthResponseHandler;
	private final SignupResponseHandler signupResponseHandler;
	private final HeaderUtil headerUtil;

	/**
	 * 카카오 OAuth 콜백
	 *
	 * 플로우:
	 * 1. Application Layer에서 비즈니스 로직 처리
	 * 2. ResponseAction 반환
	 * 3. ResponseHandler가 HTTP 응답 구성 (리다이렉트 + 쿼리파라미터)
	 */
	@GetMapping("/login/oauth2/code/kakao")
	public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

		// 1. Application Layer: 비즈니스 로직
		AuthResult.OAuthResponseAction action = authFacade.handleOAuthLogin(code);

		// 2. ResponseHandler: HTTP 응답 구성 (리다이렉트)
		oauthResponseHandler.handle(action, response);
	}

	/**
	 * 회원가입 완료
	 *
	 * SignupToken (쿼리파라미터) + 추가정보(닉네임)
	 */
	@PostMapping("/signup/complete")
	public ResponseEntity<ApiResponse<SignupResponseHandler.SignupCompleteResponse>> completeSignup(
		@RequestParam("signupToken") String signupToken,
		@Valid @RequestBody AuthCriteria.AdditionalSignUpData request) {

		AuthResult.SignupResponseAction action = authFacade.completeSignup(signupToken, request);

		return signupResponseHandler.handle(action);
	}

	/**
	 * JWT 교환 API
	 *
	 * AuthExchangeToken → JWT 교환
	 * 응답: Authorization Header (Access Token), X-Refresh-Token Header (Refresh Token)
	 */
	@PostMapping("/token/exchange")
	public ApiResponse<Void> exchangeAuthToken(@Valid @RequestBody TokenExchangeRequest request,
		HttpServletResponse response) {

		AuthInfo.TokenPair tokens = authFacade.exchangeAuthToken(request.authCode());

		headerUtil.addJwtHeaders(response, tokens);

		return ApiResponse.ok();

	}

	/**
	 * JWT 갱신 (Refresh Token Rotation)
	 *
	 * 요청: X-Refresh-Token 헤더
	 * 응답: Authorization Header (새 Access Token), X-Refresh-Token Header (새 Refresh Token)
	 */
	@PostMapping("/token/refresh")
	public ApiResponse<Void> refreshToken(
		@RequestHeader("X-Refresh-Token") String refreshToken,
		HttpServletResponse response) {

		AuthInfo.TokenPair newTokens = authFacade.refreshTokens(refreshToken);

		headerUtil.addJwtHeaders(response, newTokens);

		return ApiResponse.ok();
	}

	/**
	 * 로그아웃
	 *
	 * 요청: X-Refresh-Token 헤더
	 */
	@PostMapping("/logout")
	public ApiResponse<Void> logout(@RequestHeader("X-Refresh-Token") String refreshToken) {
		authFacade.logout(refreshToken);
		return ApiResponse.ok();
	}

	// ===== Request DTOs =====
	public record TokenExchangeRequest(
		@NotBlank String authCode
	) {
	}
}
