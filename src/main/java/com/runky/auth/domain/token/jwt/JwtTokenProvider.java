package com.runky.auth.domain.token.jwt;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.token.jwt.component.JwtTokenGenerator;
import com.runky.auth.domain.token.jwt.component.JwtTokenParser;
import com.runky.auth.domain.token.jwt.component.JwtTokenStore;
import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;
import com.runky.auth.domain.token.jwt.vo.TokenClaims;
import com.runky.auth.exception.domain.InvalidTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
	private final JwtTokenGenerator tokenGenerator;
	private final JwtTokenParser tokenParser;
	private final JwtTokenStore tokenStore;

	@Transactional
	public JwtTokenPair createTokenPair(Long memberId, String role) {
		JwtTokenPair tokens = tokenGenerator.generateTokenPair(memberId, role);

		tokenStore.saveRefreshToken(
			memberId,
			tokens.refreshToken(),
			tokens.refreshTokenIssuedAt(),
			tokens.refreshTokenExpiresAt()
		);

		log.info("JWT token pair created for memberId: {}", memberId);
		return tokens;
	}

	@Transactional
	public JwtTokenPair rotateTokens(String refreshToken) {
		if (!validateRefreshToken(refreshToken)) {
			throw new InvalidTokenException();
		}
		TokenClaims claims = tokenParser.parseRefreshToken(refreshToken);
		tokenStore.verifyRefreshTokenHash(claims.memberId(), refreshToken);
		JwtTokenPair newTokens = tokenGenerator.generateTokenPair(claims.memberId(), claims.role());

		// 5. Refresh Token 교체 (해시 업데이트)
		tokenStore.rotateRefreshToken(
			claims.memberId(),
			refreshToken,
			newTokens.refreshToken(),
			newTokens.refreshTokenIssuedAt(),
			newTokens.refreshTokenExpiresAt()
		);

		log.info("JWT tokens rotated for memberId: {}", claims.memberId());
		return newTokens;
	}

	@Transactional
	public void revokeTokens(Long memberId) {
		tokenStore.deleteRefreshToken(memberId);
		log.info("JWT tokens revoked for memberId: {}", memberId);
	}

	public boolean validateAccessToken(String accessToken) {
		return tokenParser.validateAccessToken(accessToken);
	}

	public boolean validateRefreshToken(String refreshToken) {
		if (!tokenParser.validateRefreshToken(refreshToken)) {
			return false;
		}

		// DB에 존재하는지 확인
		TokenClaims claims = tokenParser.parseRefreshToken(refreshToken);
		return tokenStore.existsRefreshToken(claims.memberId(), refreshToken);
	}

	// ===== 파싱 =====
	public Long getMemberIdFromAccessToken(String accessToken) {
		return tokenParser.parseAccessToken(accessToken).memberId();
	}

	public TokenClaims parseRefreshToken(String refreshToken) {
		return tokenParser.parseRefreshToken(refreshToken);
	}

	public Long getMemberIdFromRefreshToken(String refreshToken) {
		return tokenParser.parseRefreshToken(refreshToken).memberId();
	}

}
