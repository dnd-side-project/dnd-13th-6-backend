package com.runky.auth.domain.token.refresh;

import java.time.Instant;

import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.auth.exception.domain.TokenRequiredException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "refresh_token",
	indexes = {
		@Index(name = "ux_refresh_token_member_id", columnList = "member_id"),
		@Index(name = "ix_refresh_token_expires_at", columnList = "expires_at")
	}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "token_hash", nullable = false, length = 64)
	private String tokenHash;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Builder
	private RefreshToken(final Long memberId, final String tokenHash, final Instant createdAt,
		final Instant expiresAt) {

		if (tokenHash == null || tokenHash.isBlank()) {
			throw new TokenRequiredException();
		}

		if (expiresAt.isBefore(Instant.now())) {
			throw new ExpiredTokenException();
		}
		if (tokenHash.length() > 64) {
			throw new InvalidTokenException();
		}

		this.memberId = memberId;
		this.tokenHash = tokenHash;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public static RefreshToken issue(Long memberId, String tokenHash, Instant createdAt, Instant expiresAt) {

		return RefreshToken.builder()
			.memberId(memberId)
			.tokenHash(tokenHash)
			.createdAt(createdAt)
			.expiresAt(expiresAt)
			.build();
	}

	public void rotateTo(String tokenHash, Instant createdAt, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}
}
