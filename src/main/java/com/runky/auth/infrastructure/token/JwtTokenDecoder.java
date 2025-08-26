package com.runky.auth.infrastructure.token;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.domain.vo.RefreshTokenClaims;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenDecoder implements TokenDecoder {

	private final JwtSigningKeyProvider signingKeyProvider;

	@Override
	public AccessTokenClaims decodeAccess(String accessToken) {
		if (accessToken == null || accessToken.isBlank()) {
			throw new InvalidTokenException();
		}
		try {
			SecretKey key = signingKeyProvider.accessKey();

			Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(accessToken)
				.getPayload();

			Long memberId = Long.valueOf(claims.getSubject());
			String role = claims.get("role", String.class);

			return new AccessTokenClaims(memberId, role);
		} catch (ExpiredJwtException e) {
			throw new ExpiredTokenException();
		} catch (JwtException | IllegalArgumentException e) {
			throw new InvalidTokenException();
		}
	}

	@Override
	public RefreshTokenClaims decodeRefresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new InvalidTokenException();
		}
		try {
			SecretKey key = signingKeyProvider.refreshKey();

			Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(refreshToken)
				.getPayload();

			Long memberId = Long.valueOf(claims.getSubject());
			String role = claims.get("role", String.class);
			Date exp = claims.getExpiration();

			return new RefreshTokenClaims(memberId, role, exp.toInstant());
		} catch (ExpiredJwtException e) {
			throw new ExpiredTokenException();
		} catch (JwtException | IllegalArgumentException e) {
			throw new InvalidTokenException();
		}
	}
}
