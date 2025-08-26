package com.runky.global.security.filter;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.global.security.auth.MemberPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 필터가 직접 JWT(accessToken)를 검증하고 SecurityContextHolder에 인증을 넣는다.
 * - 쿠키(accessToken)를 대상으로함.
 * - Stateless: 요청 종료 시 컨텍스트는 폐기된다.
 */
public class JwtCookieAuthFilter extends OncePerRequestFilter {

	//TODO: auth-controller의 "accessToken"와 함께 연동. 공용 상수화 필요
	private static final String ACCESS_TOKEN_COOKIE = "accessToken";
	private final TokenDecoder tokenDecoder;

	public JwtCookieAuthFilter(TokenDecoder tokenDecoder) {
		this.tokenDecoder = tokenDecoder;
	}

	// TODO: 쿠키 없을 때, 예외처리
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {
		try {
			// 1. 쿠키에서 인증토큰 가져옴
			String token = resolveFromCookie(req);
			// 2. 인증객체 생성 -> 시큐리티 컨텍스트에 저장
			if (StringUtils.hasText(token)) {
				AccessTokenClaims claims = tokenDecoder.decodeAccess(token);
				MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());

				var authentication = new UsernamePasswordAuthenticationToken(
					principal, null, principal.authorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(req, res);
	}

	private String resolveFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		return Arrays.stream(cookies)
			.filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

}
