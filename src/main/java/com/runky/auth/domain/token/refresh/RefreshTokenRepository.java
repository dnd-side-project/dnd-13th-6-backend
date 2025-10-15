package com.runky.auth.domain.token.refresh;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository {
	void deleteByMemberId(Long memberId);

	void save(RefreshToken issue);

	int deleteExpiredBefore(Instant now);

	Optional<RefreshToken> findByMemberIdAndTokenHash(Long memberId, String tokenHash);
}
