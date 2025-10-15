package com.runky.auth.domain;

import java.time.Instant;

import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;

public final class AuthInfo {
	private AuthInfo() {
	}

	public record TokenPair(String accessToken, String refreshToken,
							Instant accessTokenIssuedAt, Instant accessTokenExpiresAt,
							Instant refreshTokenIssuedAt, Instant refreshTokenExpiresAt) {
		public static TokenPair from(JwtTokenPair pair) {
			return new TokenPair(pair.accessToken(), pair.refreshToken(),
				pair.accessTokenIssuedAt(), pair.accessTokenExpiresAt(),
				pair.refreshTokenIssuedAt(), pair.refreshTokenExpiresAt());
		}
	}

}
