package com.runky.auth.domain.token.jwt.vo;

import java.time.Instant;

import lombok.Builder;

@Builder
public record JwtTokenPair(
	String accessToken,
	String refreshToken,
	Instant accessTokenIssuedAt,
	Instant accessTokenExpiresAt,
	Instant refreshTokenIssuedAt,
	Instant refreshTokenExpiresAt
) {
}
