package com.runky.auth.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.runky.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthResponseHelper {

	/** 본문 + 쿠키 */
	public <T> ApiResponse<T> successWithCookies(
		ApiResponse<T> body,
		List<ResponseCookie> cookies,
		HttpServletResponse response
	) {
		// 같은 이름 쿠키가 있다면 "마지막 것"만 남긴다.
		for (ResponseCookie cookie : dedupByName(cookies)) {
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		}
		return body;
	}

	/** 쿠키만 (본문은 OK) */
	public ApiResponse<Void> successWithCookies(List<ResponseCookie> cookies, HttpServletResponse resp) {
		for (ResponseCookie cookie : dedupByName(cookies)) {
			resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		}
		return ApiResponse.ok();
	}

	/** 이름 기준 중복 제거 (뒤에 온 쿠키 우선) */
	private List<ResponseCookie> dedupByName(List<ResponseCookie> cookies) {
		Map<String, ResponseCookie> map = new LinkedHashMap<>();
		for (ResponseCookie c : cookies) {
			map.put(c.getName(), c);
		}
		return List.copyOf(map.values());
	}

	public <T> ApiResponse<T> successWithCookiesAndRedirect(
		ApiResponse<T> body,
		List<ResponseCookie> cookies,
		String location,
		HttpServletResponse response
	) {
		return successWithCookiesAndRedirect(body, cookies, location, HttpStatus.SEE_OTHER, response);
	}

	public <T> ApiResponse<T> successWithCookiesAndRedirect(
		ApiResponse<T> body,
		List<ResponseCookie> cookies,
		String location,
		HttpStatus status,
		HttpServletResponse response
	) {
		for (ResponseCookie cookie : dedupByName(cookies)) {
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		}
		response.setStatus(status.value());
		response.setHeader(HttpHeaders.LOCATION, location);
		return body;
	}
	//

	/**
	 * 본문 + 쿠키를 포함한 ResponseEntity 생성
	 */
	public <T> ResponseEntity<ApiResponse<T>> successWithCookies(
		ApiResponse<T> body,
		List<ResponseCookie> cookies
	) {
		HttpHeaders headers = new HttpHeaders();
		dedupByName(cookies).forEach(cookie -> headers.add(HttpHeaders.SET_COOKIE, cookie.toString()));

		return ResponseEntity.ok()
			.headers(headers)
			.body(body);
	}

	/**
	 * 본문 + 쿠키 + 리다이렉션을 포함한 ResponseEntity 생성
	 */
	public ResponseEntity<ApiResponse<?>> successWithCookiesAndRedirect(
		ApiResponse<?> body,
		List<ResponseCookie> cookies,
		String location
	) {
		HttpHeaders headers = new HttpHeaders();
		dedupByName(cookies).forEach(cookie -> headers.add(HttpHeaders.SET_COOKIE, cookie.toString()));
		headers.setLocation(URI.create(location));

		return ResponseEntity.status(HttpStatus.SEE_OTHER)
			.headers(headers)
			.body(body);
	}

}
