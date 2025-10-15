package com.runky.auth.domain.token.jwt.component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.runky.auth.config.props.JwtProperties;
import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;
import com.runky.auth.exception.infra.JwtAlgorithmUnsupportedException;
import com.runky.auth.infrastructure.token.JwtSigningKeyProvider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {

	private final JwtProperties jwtProperties;
	private final JwtSigningKeyProvider signingKeyProvider;

	public JwtTokenPair generateTokenPair(Long memberId, String role) {
		Instant now = Instant.now();

		// Access Token
		Instant accessExpiry = now.plus(jwtProperties.access().ttl());
		String accessToken = buildAccessToken(memberId, role, now, accessExpiry);

		// Refresh Token
		Instant refreshExpiry = now.plus(jwtProperties.refresh().ttl());
		String refreshToken = buildRefreshToken(memberId, role, now, refreshExpiry);

		return JwtTokenPair.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenIssuedAt(now)
			.accessTokenExpiresAt(accessExpiry)
			.refreshTokenIssuedAt(now)
			.refreshTokenExpiresAt(refreshExpiry)
			.build();
	}

	private String buildAccessToken(Long memberId, String role, Instant issuedAt, Instant expiresAt) {
		SecretKey secretKey = signingKeyProvider.accessKey();
		MacAlgorithm algorithm = resolveAlgorithm(jwtProperties.access().algorithm());

		return Jwts.builder()
			.id(UUID.randomUUID().toString())
			.subject(String.valueOf(memberId))
			.issuedAt(Date.from(issuedAt))
			.expiration(Date.from(expiresAt))
			.claim("role", role)
			.claim("type", "ACCESS")
			.signWith(secretKey, algorithm)
			.compact();
	}

	private String buildRefreshToken(Long memberId, String role, Instant issuedAt, Instant expiresAt) {
		SecretKey secretKey = signingKeyProvider.refreshKey();
		MacAlgorithm algorithm = resolveAlgorithm(jwtProperties.refresh().algorithm());

		return Jwts.builder()
			.id(UUID.randomUUID().toString())
			.subject(String.valueOf(memberId))
			.issuedAt(Date.from(issuedAt))
			.expiration(Date.from(expiresAt))
			.claim("role", role)
			.claim("type", "REFRESH")
			.signWith(secretKey, algorithm)
			.compact();
	}

	private MacAlgorithm resolveAlgorithm(String name) {
		return switch (name) {
			case "HS256" -> Jwts.SIG.HS256;
			case "HS384" -> Jwts.SIG.HS384;
			case "HS512" -> Jwts.SIG.HS512;
			default -> throw new JwtAlgorithmUnsupportedException();
		};
	}
}
