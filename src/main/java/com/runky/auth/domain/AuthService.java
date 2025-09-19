package com.runky.auth.domain;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;
import com.runky.global.security.auth.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final TokenDecoder tokenDecoder;

	public Authentication authenticate(String jwt) {

		AccessTokenClaims claims = tokenDecoder.decodeAccess(jwt);
		MemberPrincipal principal = new MemberPrincipal(claims.memberId(), claims.role());
		return new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
	}

	public MemberPrincipal principalOf(Authentication auth) {
		if (auth == null) {
			throw new GlobalException(AuthErrorCode.NULL_AUTHENTICATION);
		}
		return (MemberPrincipal)auth.getPrincipal();
	}
}
