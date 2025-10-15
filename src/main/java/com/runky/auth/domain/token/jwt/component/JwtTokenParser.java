package com.runky.auth.domain.token.jwt.component;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.token.jwt.vo.TokenClaims;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.auth.infrastructure.token.JwtSigningKeyProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenParser {
	private final JwtSigningKeyProvider signingKeyProvider;

	public boolean validateAccessToken(String accessToken) {
		try {
			parseAccessToken(accessToken);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateRefreshToken(String refreshToken) {
		try {
			parseRefreshToken(refreshToken);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public TokenClaims parseAccessToken(String accessToken) {
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

			return extractClaims(claims, "ACCESS");
		} catch (ExpiredJwtException e) {
			throw new ExpiredTokenException();
		} catch (JwtException | IllegalArgumentException e) {
			throw new InvalidTokenException();
		}
	}

	public TokenClaims parseRefreshToken(String refreshToken) {
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

			return extractClaims(claims, "REFRESH");
		} catch (ExpiredJwtException e) {
			throw new ExpiredTokenException();
		} catch (JwtException | IllegalArgumentException e) {
			throw new InvalidTokenException();
		}
	}

	private TokenClaims extractClaims(Claims claims, String expectedType) {
		Long memberId = Long.valueOf(claims.getSubject());
		String role = claims.get("role", String.class);
		String type = claims.get("type", String.class);

		if (!expectedType.equals(type)) {
			throw new InvalidTokenException();
		}

		return new TokenClaims(memberId, role);
	}
}
