package com.runky.dev;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runky.auth.api.AuthRequest;
import com.runky.auth.application.AuthCriteria;
import com.runky.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dev/api/auth")
@RequiredArgsConstructor
public class DevAuthController {
	private final DevAuthFacade authFacade;
	private final DevAuthResponseHelper responseHelper;

	/**
	 * 카카오 콜백: code 교환 후 분기
	 * - NEW_USER: signupToken 쿠키 + AuthResponse.NewUser
	 * - EXISTING_USER: AT/RT 쿠키 + AuthResponse.ExistingUser
	 */
	@GetMapping("/login/oauth2/code/kakao")
	public ResponseEntity<ApiResponse<DevAuthResponse>> devKakaoCallback(@RequestParam("code") String code) {
		var result = authFacade.handleOAuthLogin(code);

		return switch (result.authStatus()) {
			case NEW_USER -> responseHelper.redirectWithFragment(
				ApiResponse.success(new DevAuthResponse.NewUser(result.signupToken())),
				"https://localhost:3000/onboarding/terms",
				Map.of("next", "COMPLETE_SIGNUP", "signupToken", result.signupToken())
			);
			case EXISTING_USER -> responseHelper.redirectWithFragment(
				ApiResponse.success(new DevAuthResponse.ExistingUser(result.accessToken(), result.refreshToken())),
				"https://localhost:3000/main",
				Map.of("accessToken", result.accessToken(), "refreshToken", result.refreshToken())
			);
		};
	}

	/** 회원가입 완료 → AT/RT를 응답 헤더로 반환 */
	@PostMapping("/signup/complete")
	public ResponseEntity<ApiResponse<Void>> devCompleteSignup(
		@RequestHeader("X-Signup-Token") String signupToken,
		@RequestBody AuthRequest.Signup request
	) {
		var result = authFacade.completeSignup(
			signupToken, new AuthCriteria.AdditionalSignUpData(request.nickname())
		);
		return responseHelper.successWithHeaders(
			ApiResponse.ok(),
			Map.of("X-Access-Token", result.accessToken(),
				"X-Refresh-Token", result.refreshToken())
		);
	}

	/** RT로 재발급 → AT/RT를 응답 헤더로 반환 */
	@PostMapping("/token/refresh")
	public ResponseEntity<ApiResponse<Void>> devRefresh(
		@RequestHeader("X-Refresh-Token") String refreshToken
	) {
		var rotated = authFacade.reissueByRefresh(refreshToken);
		return responseHelper.successWithHeaders(
			ApiResponse.ok(),
			Map.of("X-Access-Token", rotated.accessToken(),
				"X-Refresh-Token", rotated.refreshToken())
		);
	}

	/** 로그아웃 → 헤더만 사용 (쿠키 제거 없음) */
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> devLogout(
		@RequestHeader("X-Refresh-Token") String refreshToken
	) {
		authFacade.logoutByRefresh(refreshToken);
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
