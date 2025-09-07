package com.runky.global.security.filter;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.TokenRequiredException;
import com.runky.global.error.GlobalErrorCode;
import com.runky.global.security.auth.MemberPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 필터가 직접 JWT(accessToken)를 검증하고 SecurityContextHolder에 인증을 넣는다. - 쿠키(accessToken)를 대상으로함. - Stateless: 요청 종료 시 컨텍스트는
 * 폐기된다.
 */
@Slf4j
public class JwtCookieAuthFilter extends OncePerRequestFilter {

	//TODO: auth-controller의 "accessToken"와 함께 연동. 공용 상수화 필요
	private static final String ACCESS_TOKEN_COOKIE = "accessToken";
	private static final String HDR_AUTHORIZATION = "Authorization";
	private static final String HDR_X_ACCESS_TOKEN = "X-Access-Token";
	private static final String BEARER_PREFIX = "Bearer ";
	private final TokenDecoder tokenDecoder;

	public JwtCookieAuthFilter(TokenDecoder tokenDecoder) {
		this.tokenDecoder = tokenDecoder;
	}

	// TODO: 쿠키 없을 때, 예외처리
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {
		// 1) 쿠키에서 우선 조회
		String token = resolveFromCookie(req);

		// 2) 쿠키가 없거나 비어있으면 헤더로 대체
		if (!StringUtils.hasText(token)) {
			token = resolveFromHeader(req);
		}

		if (StringUtils.hasText(token)) {
			try {
				AccessTokenClaims claims = tokenDecoder.decodeAccess(token);
				MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());

				var authentication = new UsernamePasswordAuthenticationToken(
					principal, null, principal.authorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ExpiredTokenException e) {
				req.setAttribute("exception", GlobalErrorCode.EXPIRED_TOKEN);
			} catch (TokenRequiredException e) {
				req.setAttribute("exception", GlobalErrorCode.INVALID_TOKEN);
			}
		} else {
			req.setAttribute("exception", GlobalErrorCode.NOT_LOGIN_MEMBER);
		}

		chain.doFilter(req, res);
	}

	private String resolveFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		return Arrays.stream(cookies)
			.filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

	private String resolveFromHeader(HttpServletRequest request) {
		// 1) Authorization: Bearer <token>
		String auth = request.getHeader(HDR_AUTHORIZATION);
		if (StringUtils.hasText(auth) && auth.startsWith(BEARER_PREFIX)) {
			return auth.substring(BEARER_PREFIX.length()).trim();
		}
		// 2) X-Access-Token: <token> (응답/요청 모두에서 사용하기 편리한 보조 헤더)
		String x = request.getHeader(HDR_X_ACCESS_TOKEN);
		return StringUtils.hasText(x) ? x.trim() : null;
	}

}
