package com.runky.auth.interfaces;

import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runky.auth.application.AuthCriteria;
import com.runky.auth.application.AuthFacade;
import com.runky.auth.application.AuthResult;
import com.runky.auth.config.props.LoginRedirectProperties;
import com.runky.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiSpec {
	private final LoginRedirectProperties loginRedirectProperties;
	private final AuthFacade authFacade;
	private final TokenCookieProvider cookieProvider;
	private final AuthResponseHelper responseHelper;

	/**
	 * 카카오 콜백: code 교환 후 분기
	 * - NEW_USER: signupToken 쿠키 + AuthResponse.NewUser
	 * - EXISTING_USER: AT/RT 쿠키 + AuthResponse.ExistingUser
	 */
	//	@GetMapping("/login/oauth2/code/kakao")
	//	public ApiResponse<AuthResponse> kakaoCallback(
	//		@RequestParam("code") String code,
	//		HttpServletResponse servletResponse
	//	) {
	//		AuthResult.OAuthLogin result = authFacade.handleOAuthLogin(code);
	//
	//		return switch (result.authStatus()) {
	//			case NEW_USER -> {
	//				ResponseCookie st = cookieProvider.signupToken(result.signupToken());
	//				yield responseHelper.successWithCookiesAndRedirect(
	//					ApiResponse.success(new AuthResponse.NewUser()),
	//					List.of(st),
	//					NEW_USER_REDIRECT,
	//					servletResponse
	//				);
	//			}
	//			case EXISTING_USER -> {
	//				ResponseCookie at = cookieProvider.accessToken(result.accessToken());
	//				ResponseCookie rt = cookieProvider.refreshToken(result.refreshToken());
	//				yield responseHelper.successWithCookies(
	//					ApiResponse.success(new AuthResponse.ExistingUser()),
	//					List.of(at, rt),
	//					servletResponse
	//				);
	//			}
	//		};
	//	}
	@GetMapping("/login/oauth2/code/kakao")
	public ResponseEntity<ApiResponse<?>> kakaoCallback(
		@RequestParam("code") String code
	) {
		AuthResult.OAuthLogin result = authFacade.handleOAuthLogin(code);

		return switch (result.authStatus()) {
			case NEW_USER -> {
				ResponseCookie st = cookieProvider.signupToken(result.signupToken());
				yield responseHelper.successWithCookiesAndRedirect(
					ApiResponse.success(new AuthResponse.NewUser()),
					List.of(st),
					loginRedirectProperties.newUser()
				);
			}
			case EXISTING_USER -> {
				ResponseCookie at = cookieProvider.accessToken(result.accessToken());
				ResponseCookie rt = cookieProvider.refreshToken(result.refreshToken());
				yield responseHelper.successWithCookiesAndRedirect(
					ApiResponse.success(new AuthResponse.ExistingUser()),
					List.of(at, rt),
					loginRedirectProperties.alreadyExistingUser()
				);
			}
		};
	}

	/**
	 * 회원가입 완료: signupToken(쿠키)로 최종 등록 → AT/RT 발급
	 */
	@PostMapping("/signup/complete")
	public ApiResponse<Void> completeSignup(
		@CookieValue("signupToken") String signupToken,
		@RequestBody AuthRequest.Signup request,
		HttpServletResponse servletResponse
	) {
		AuthResult.SigninComplete result = authFacade.completeSignup(signupToken,
			new AuthCriteria.AdditionalSignUpData(request.nickname()));

		ResponseCookie st = cookieProvider.delete("signupToken");

		ResponseCookie at = cookieProvider.accessToken(result.accessToken());
		ResponseCookie rt = cookieProvider.refreshToken(result.refreshToken());

		return responseHelper.successWithCookies(List.of(st, at, rt), servletResponse);
	}

	/**
	 * 토큰 재발급: RT로 AT/RT 재발급
	 */
	@PostMapping("/token/refresh")
	public ApiResponse<Void> refresh(
		@CookieValue("refreshToken") String refreshToken,
		HttpServletResponse servletResponse
	) {
		AuthResult.rotatedToken rotated = authFacade.reissueByRefresh(refreshToken);

		ResponseCookie at = cookieProvider.accessToken(rotated.accessToken());
		ResponseCookie rt = cookieProvider.refreshToken(rotated.refreshToken());

		return responseHelper.successWithCookies(List.of(at, rt), servletResponse);
	}

	/**
	 * 로그아웃: RT 로그아웃 처리 후 AT/RT/ST 쿠키 제거
	 */
	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		@CookieValue("refreshToken") String refreshToken,
		HttpServletResponse servletResponse
	) {
		authFacade.logoutByRefresh(refreshToken);

		ResponseCookie clearAT = cookieProvider.delete("accessToken");
		ResponseCookie clearRT = cookieProvider.delete("refreshToken");
		ResponseCookie clearST = cookieProvider.delete("signupToken");

		return responseHelper.successWithCookies(List.of(clearAT, clearRT, clearST), servletResponse);
	}
}
