package com.runky.developer;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.runky.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class DevAuthResponseHelper {

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
	public <T> ResponseEntity<ApiResponse<T>> successWithCookiesAndRedirect(
		ApiResponse<T> body,
		List<ResponseCookie> cookies,
		String location
	) {
		HttpHeaders headers = new HttpHeaders();
		dedupByName(cookies).forEach(cookie -> headers.add(HttpHeaders.SET_COOKIE, cookie.toString()));
		headers.setLocation(URI.create(location)); // 리다이렉션 URI 설정

		// 303 See Other는 POST 요청 후 GET으로 리다이렉트할 때 주로 사용됩니다.
		// OAuth 콜백은 GET이므로 302 Found도 괜찮습니다.
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
			.headers(headers)
			.body(body);
	}

	public <T> ResponseEntity<ApiResponse<T>> redirectWithFragment(
		ApiResponse<T> body,
		String baseLocation,
		Map<String, String> fragmentParams
	) {
		String frag = fragmentParams.entrySet().stream()
			.map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
				+ URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
			.collect(Collectors.joining("&"));

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create(baseLocation + "#" + frag));
		headers.add(HttpHeaders.CACHE_CONTROL, "no-store");
		headers.add("Referrer-Policy", "no-referrer");

		return ResponseEntity.status(HttpStatus.SEE_OTHER)
			.headers(headers)
			.body(body);
	}

	public <T> ResponseEntity<ApiResponse<T>> successWithHeaders(
		ApiResponse<T> body,
		Map<String, String> headers
	) {
		HttpHeaders h = new HttpHeaders();
		if (headers != null)
			headers.forEach(h::add);

		// 크로스 도메인에서 JS가 커스텀 헤더를 읽을 수 있도록 노출
		if (headers != null && !headers.isEmpty()) {
			h.add("Access-Control-Expose-Headers", String.join(",", headers.keySet()));
		}
		// 캐시/리퍼러 최소화
		h.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
		h.add("Pragma", "no-cache");
		h.add("Referrer-Policy", "no-referrer");

		return ResponseEntity.ok().headers(h).body(body);
	}

}
