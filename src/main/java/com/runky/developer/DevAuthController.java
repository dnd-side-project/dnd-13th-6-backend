package com.runky.developer;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runky.auth.application.AuthCriteria;
import com.runky.auth.interfaces.AuthRequest;
import com.runky.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dev/api/auth")
@RequiredArgsConstructor
public class DevAuthController implements DevAuthApiSpec {
	private final DevAuthFacade authFacade;
	private final DevAuthResponseHelper responseHelper;

	@GetMapping("/login/oauth2/code/kakao/{branch}")
	public ResponseEntity<ApiResponse<DevAuthResponse>> devKakaoCallback(
		@RequestParam("code") String code,
		@PathVariable("branch") String branch
	) {
		var result = authFacade.handleOAuthLogin(code, branch);
		String domain = null;
		switch (branch) {
			case "local" -> {
				domain = "localhost:3000";
			}
			case "dev" -> {
				domain = "test.runky.store";
			}
		}
		String NEW_USER_REDIRECT_URL = "https://" + domain + "/onboarding/terms";
		String EXISTING_USER_REDIRECT_URL = "https://" + domain + "/main";

		return switch (result.authStatus()) {
			case NEW_USER -> responseHelper.redirectWithFragment(
				ApiResponse.success(new DevAuthResponse.NewUser(result.signupToken())),
				NEW_USER_REDIRECT_URL,
				Map.of("next", "COMPLETE_SIGNUP", "signupToken", result.signupToken())
			);
			case EXISTING_USER -> responseHelper.redirectWithFragment(
				ApiResponse.success(new DevAuthResponse.ExistingUser(result.accessToken(), result.refreshToken())),
				EXISTING_USER_REDIRECT_URL,
				Map.of("accessToken", result.accessToken(), "refreshToken", result.refreshToken())
			);
		};
	}

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

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> devLogout(
		@RequestHeader("X-Refresh-Token") String refreshToken
	) {
		authFacade.logoutByRefresh(refreshToken);
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
