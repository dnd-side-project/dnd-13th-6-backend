package com.runky.auth.application;

public final class AuthResult {
	private AuthResult() {
	}

	public sealed interface OAuthResponseAction
		permits OAuthResponseAction.NewUserRedirect, OAuthResponseAction.ExistingUserRedirect {

		/**
		 * 신규 회원 - 회원가입 페이지로 리다이렉트
		 * SignupToken을 HttpOnly Cookie로 전달
		 */
		record NewUserRedirect(String signupToken) implements OAuthResponseAction {
		}

		/**
		 * 기존 회원 - 메인 페이지로 리
		 * 다이렉트
		 * AuthExchangeToken을 쿼리파라미터로 전달
		 */
		record ExistingUserRedirect(String authExchangeToken) implements OAuthResponseAction {
		}
	}

	public sealed interface SignupResponseAction permits SignupResponseAction.SignupCompleteResponse {

		/**
		 * 회원가입 완료 - AuthExchangeToken 반환
		 */
		record SignupCompleteResponse(String authExchangeToken) implements SignupResponseAction {
		}
	}

}
