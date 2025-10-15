package com.runky.auth.interfaces;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.runky.auth.application.AuthResult;
import com.runky.auth.config.props.LoginRedirectProperties;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthResponseHandler {
	private final LoginRedirectProperties loginRedirectProperties;

	/**
	 * OAuthResponseAction에 따라 적절한 응답 처리
	 */
	public void handle(AuthResult.OAuthResponseAction action, HttpServletResponse response) {

		if (action instanceof AuthResult.OAuthResponseAction.NewUserRedirect newUser) {
			handleNewUserRedirect(newUser.signupToken(), response);

		} else if (action instanceof AuthResult.OAuthResponseAction.ExistingUserRedirect existing) {
			handleExistingUserRedirect(existing.authExchangeToken(), response);
		}
	}

	private void handleNewUserRedirect(String signupToken, HttpServletResponse response) {

		String redirectUrl = loginRedirectProperties.newUser() + "?signupToken=" + signupToken;
		try {
			response.sendRedirect(redirectUrl);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleExistingUserRedirect(String authExchangeToken, HttpServletResponse response) {

		String redirectUrl = loginRedirectProperties.alreadyExistingUser() + "?code=" + authExchangeToken;
		try {
			response.sendRedirect(redirectUrl);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
