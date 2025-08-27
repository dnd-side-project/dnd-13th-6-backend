package com.runky.auth.api;

import java.time.Duration;
import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.runky.auth.config.props.AuthCookieProperties;
import com.runky.auth.config.props.JwtProperties;
import com.runky.auth.config.props.SignupTokenProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenCookieProvider {
	private final AuthCookieProperties cookieProps;
	private final JwtProperties jwtProps;
	private final SignupTokenProperties signupProps;

	public ResponseCookie accessToken(String v) {
		Duration ttl = cookieProps.access().maxAgeOr(jwtProps.access().ttl());
		return build(cookieProps.access().name(), v, ttl);
	}

	public ResponseCookie refreshToken(String v) {
		Duration ttl = cookieProps.refresh().maxAgeOr(jwtProps.refresh().ttl());
		return build(cookieProps.refresh().name(), v, ttl);
	}

	public ResponseCookie signupToken(String v) {
		Duration ttl = cookieProps.signup().maxAgeOr(signupProps.ttl());
		return build(cookieProps.signup().name(), v, ttl);
	}

	public ResponseCookie delete(String name) {
		return base(name, "")
			.maxAge(Duration.ZERO)
			.build();
	}

	private ResponseCookie build(String name, String val, Duration maxAge) {
		return base(name, val).maxAge(maxAge).build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String name, String val) {
		return ResponseCookie.from(name, val)
			.httpOnly(cookieProps.common().httpOnly())
			.secure(cookieProps.common().secure())
			.path(cookieProps.common().path())
			//			.domain(cookieProps.common().domain())
			.sameSite(cookieProps.common().sameSite());
	}

	/** 과거 변형까지 함께 제거: host-only, configured-domain 둘 다 삭제 쿠키 생성 */
	public List<ResponseCookie> deleteAllVariants(String name) {
		ResponseCookie hostOnly = ResponseCookie.from(name, "")
			.httpOnly(cookieProps.common().httpOnly())
			.secure(cookieProps.common().secure())
			.sameSite(cookieProps.common().sameSite())
			.path(cookieProps.common().path())
			// domain 생략(HostOnly) 변형 삭제
			.maxAge(Duration.ZERO)
			.build();

		ResponseCookie withDomain = ResponseCookie.from(name, "")
			.httpOnly(cookieProps.common().httpOnly())
			.secure(cookieProps.common().secure())
			.sameSite(cookieProps.common().sameSite())
			.path(cookieProps.common().path())
			.domain(cookieProps.common().domain()) // 설정된 domain이 있으면 같은 값으로
			.maxAge(Duration.ZERO)
			.build();

		// domain이 null이면 withDomain은 hostOnly와 같으므로 dedup는 Helper에서 처리됩니다.
		return List.of(hostOnly, withDomain);
	}
}
