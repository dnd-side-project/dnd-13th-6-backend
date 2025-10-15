package com.runky.auth.domain.token.jwt.vo;

public record TokenClaims(
	Long memberId,
	String role
) {
}
