package com.runky.global.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.runky.auth.domain.token.jwt.component.JwtTokenParser;
import com.runky.auth.domain.token.jwt.vo.TokenClaims;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.TokenRequiredException;
import com.runky.global.error.GlobalErrorCode;
import com.runky.global.security.auth.MemberPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String HDR_AUTHORIZATION = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final List<String> EXCLUDE_PATHS = Arrays.asList(
		"/api/auth/**",
		"/actuator/health",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	);
	private final JwtTokenParser jwtTokenParser;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		boolean shouldNotFilter = EXCLUDE_PATHS.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, path));

		if (shouldNotFilter) {
			log.debug("Skipping JWT filter for path: {}", path);
		}

		return shouldNotFilter;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {
		String token = resolveFromHeader(req);

		if (StringUtils.hasText(token)) {
			try {
				TokenClaims claims = jwtTokenParser.parseAccessToken(token);
				MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());

				var authentication = new UsernamePasswordAuthenticationToken(
					principal, null, principal.authorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ExpiredTokenException e) {
				req.setAttribute("exception", GlobalErrorCode.EXPIRED_TOKEN);
			} catch (TokenRequiredException e) {
				req.setAttribute("exception", GlobalErrorCode.INVALID_TOKEN);
			}
		}

		chain.doFilter(req, res);
	}

	private String resolveFromHeader(HttpServletRequest request) {
		// Authorization: Bearer <token>
		String auth = request.getHeader(HDR_AUTHORIZATION);

		if (auth == null) {
			return null;
		}

		if (!auth.startsWith(BEARER_PREFIX)) {
			log.debug("Authorization header does not start with Bearer: {}", auth);
			return null;
		}

		String token = auth.substring(BEARER_PREFIX.length()).trim();

		return StringUtils.hasText(token) ? token : null;
	}

}
