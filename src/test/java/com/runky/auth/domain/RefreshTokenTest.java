package com.runky.auth.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.runky.auth.domain.token.refresh.RefreshToken;
import com.runky.auth.exception.domain.ExpiredTokenException;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.auth.exception.domain.TokenRequiredException;

@DisplayName("RefreshToken 엔티티 단위 테스트")
class RefreshTokenTest {

	private static String hex(char c, int len) {
		return String.valueOf(c).repeat(len); // 64자 해시 생성용
	}

	@Nested
	@DisplayName("issue")
	class Issue {

		@Test
		@DisplayName("정상 입력이면 엔티티가 생성된다")
		void issue_success() {
			// given
			Long memberId = 1L;
			Instant createdAt = Instant.now();
			Instant expiresAt = createdAt.plusSeconds(3600);
			String tokenHash = hex('a', 64);

			// when
			RefreshToken rt = RefreshToken.issue(memberId, tokenHash, createdAt, expiresAt);

			// then
			assertThat(rt.getId()).isNull(); // 영속 전
			assertThat(rt.getMemberId()).isEqualTo(memberId);
			assertThat(rt.getTokenHash()).isEqualTo(tokenHash);
			assertThat(rt.getCreatedAt()).isEqualTo(createdAt);
			assertThat(rt.getExpiresAt()).isEqualTo(expiresAt);
		}

		@Test
		@DisplayName("tokenHash == null이면 TokenRequiredException")
		void null_hash_throws() {
			Long memberId = 1L;
			Instant now = Instant.now();
			assertThrows(TokenRequiredException.class,
				() -> RefreshToken.issue(memberId, null, now, now.plusSeconds(1)));
		}

		@Test
		@DisplayName("tokenHash가 공백이면 TokenRequiredException")
		void blank_hash_throws() {
			Long memberId = 1L;
			Instant now = Instant.now();
			assertThrows(TokenRequiredException.class,
				() -> RefreshToken.issue(memberId, "   ", now, now.plusSeconds(1)));
		}

		@Test
		@DisplayName("tokenHash 길이가 64를 초과하면 InvalidTokenException")
		void too_long_hash_throws() {
			Long memberId = 1L;
			Instant now = Instant.now();
			String over = hex('b', 65);
			assertThrows(InvalidTokenException.class,
				() -> RefreshToken.issue(memberId, over, now, now.plusSeconds(1)));
		}

		@Test
		@DisplayName("expiresAt이 현재보다 과거이면 ExpiredTokenException")
		void expired_throws() {
			Long memberId = 1L;
			Instant now = Instant.now();
			assertThrows(ExpiredTokenException.class,
				() -> RefreshToken.issue(memberId, hex('c', 64), now, now.minusSeconds(1)));
		}
	}

	@Nested
	@DisplayName("rotateTo")
	class RotateTo {

		@Test
		@DisplayName("새 해쉬/시각으로 갱신된다")
		void rotate_success() {
			// given
			Long memberId = 10L;
			Instant createdAt = Instant.now();
			Instant expiresAt = createdAt.plusSeconds(3600);
			RefreshToken rt = RefreshToken.issue(memberId, hex('d', 64), createdAt, expiresAt);

			String newHash = hex('e', 64);
			Instant newCreatedAt = createdAt.plusSeconds(5);
			Instant newExpiresAt = expiresAt.plusSeconds(600);

			// when
			rt.rotateTo(newHash, newCreatedAt, newExpiresAt);

			// then
			assertThat(rt.getTokenHash()).isEqualTo(newHash);
			assertThat(rt.getCreatedAt()).isEqualTo(newCreatedAt);
			assertThat(rt.getExpiresAt()).isEqualTo(newExpiresAt);
		}

	}
}
