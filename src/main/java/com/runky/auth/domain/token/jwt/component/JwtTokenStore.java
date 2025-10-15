package com.runky.auth.domain.token.jwt.component;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.token.refresh.RefreshToken;
import com.runky.auth.domain.token.refresh.RefreshTokenHasher;
import com.runky.auth.domain.token.refresh.RefreshTokenRepository;
import com.runky.auth.exception.domain.AuthErrorCode;
import com.runky.global.error.GlobalException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenStore {
	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenHasher tokenHasher;

	public void saveRefreshToken(
		Long memberId,
		String refreshToken,
		Instant issuedAt,
		Instant expiresAt) {

		String tokenHash = tokenHasher.hash(refreshToken);

		RefreshToken entity = RefreshToken.issue(
			memberId,
			tokenHash,
			issuedAt,
			expiresAt
		);

		refreshTokenRepository.save(entity);
	}

	public boolean existsRefreshToken(Long memberId, String refreshToken) {
		String tokenHash = tokenHasher.hash(refreshToken);

		return refreshTokenRepository
			.findByMemberIdAndTokenHash(memberId, tokenHash)
			.isPresent();
	}

	public void verifyRefreshTokenHash(Long memberId, String refreshToken) {
		String tokenHash = tokenHasher.hash(refreshToken);

		refreshTokenRepository
			.findByMemberIdAndTokenHash(memberId, tokenHash)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.TOKEN_MISMATCH));
	}

	@Transactional
	public void rotateRefreshToken(
		Long memberId,
		String oldRefreshToken,
		String newRefreshToken,
		Instant newIssuedAt,
		Instant newExpiresAt) {

		String oldTokenHash = tokenHasher.hash(oldRefreshToken);

		RefreshToken current = refreshTokenRepository
			.findByMemberIdAndTokenHash(memberId, oldTokenHash)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.TOKEN_MISMATCH));

		String newTokenHash = tokenHasher.hash(newRefreshToken);
		current.rotateTo(newTokenHash, newIssuedAt, newExpiresAt);

		refreshTokenRepository.save(current);
	}

	public void deleteRefreshToken(Long memberId) {
		refreshTokenRepository.deleteByMemberId(memberId);
	}
}
