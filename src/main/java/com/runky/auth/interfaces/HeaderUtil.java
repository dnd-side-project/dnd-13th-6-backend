package com.runky.auth.interfaces;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.AuthInfo;
import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;

import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP Header 유틸리티
 * JWT 토큰을 Authorization Header로 전달
 */
@Component
public class HeaderUtil {

	/**
	 * JWT 토큰을 응답 헤더에 추가
	 *
	 * @param response HttpServletResponse
	 * @param tokens JWT 토큰 쌍
	 */
	public void addJwtHeaders(HttpServletResponse response, JwtTokenPair tokens) {
		// Access Token: Authorization 헤더
		response.setHeader("Authorization", "Bearer " + tokens.accessToken());

		// Refresh Token: X-Refresh-Token 커스텀 헤더
		response.setHeader("X-Refresh-Token", tokens.refreshToken());
	}

	/**
	 * AuthInfo.TokenPair를 응답 헤더에 추가 (오버로드)
	 */
	public void addJwtHeaders(HttpServletResponse response, AuthInfo.TokenPair tokens) {
		response.setHeader("Authorization", "Bearer " + tokens.accessToken());
		response.setHeader("X-Refresh-Token", tokens.refreshToken());
	}
}
