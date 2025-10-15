package com.runky.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.token.refresh.RefreshToken;
import com.runky.auth.domain.token.refresh.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
	private final RefreshTokenJpaRepository jpa;

	@Override
	public void deleteByMemberId(Long memberId) {
		jpa.deleteByMemberId(memberId);
	}

	@Override
	public void save(RefreshToken token) {
		jpa.save(token);
	}

	@Override
	@Transactional
	public int deleteExpiredBefore(Instant now) {
		return jpa.deleteByExpiresAtBefore(now);
	}

	@Override
	public Optional<RefreshToken> findByMemberIdAndTokenHash(final Long memberId, final String tokenHash) {
		return jpa.findByMemberIdAndTokenHash(memberId, tokenHash);
	}
}
