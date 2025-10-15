package com.runky.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runky.auth.domain.token.refresh.RefreshToken;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

	void deleteByMemberId(Long memberId);

	int deleteByExpiresAtBefore(Instant now);

	Optional<RefreshToken> findByMemberIdAndTokenHash(Long memberId, String tokenHash);
}
