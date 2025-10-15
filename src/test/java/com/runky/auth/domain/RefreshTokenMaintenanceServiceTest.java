package com.runky.auth.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.token.refresh.RefreshToken;
import com.runky.auth.domain.token.refresh.RefreshTokenRepository;
import com.runky.utils.DatabaseCleanUp;

import jakarta.persistence.EntityManager;

@SpringBootTest
class RefreshTokenMaintenanceServiceTest {

	@Autowired
	RefreshTokenMaintenanceService service;

	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@Autowired
	EntityManager em;

	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	private static String hex(char c) {
		return String.valueOf(c).repeat(64);
	}

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	private RefreshToken save(Long memberId, String hash, Instant issuedAt, Instant expiresAt) {
		RefreshToken t = RefreshToken.issue(memberId, hash, issuedAt, expiresAt);
		refreshTokenRepository.save(t);
		return t;
	}

	@Nested
	@DisplayName("만료 시점이 now 이전인 토큰만 삭제한다")
	class DeleteExpiredToken {

		@Test
		@Transactional
		@DisplayName("만료(< now)만 삭제된다")
		void deletes_only_before_now() {
			// given
			Instant base = Instant.now();
			Long memberId = 1L;

			// 생성 시점은 모두 미래로 설정하여 엔티티 가드(과거 만료 금지) 통과
			save(memberId, hex('a'), base, base.plusSeconds(10));   // < now
			save(memberId, hex('b'), base, base.plusSeconds(20));   // < now
			save(memberId, hex('c'), base, base.plusSeconds(30));   // == now
			save(memberId, hex('d'), base, base.plusSeconds(1000)); // > now

			em.flush();
			em.clear();

			Instant nowForDelete = base.plusSeconds(30);

			// when
			int deleted = service.deleteExpiredUntil(nowForDelete);

			// then
			assertThat(deleted).isEqualTo(2); // a, b 삭제

			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('a'))).isEmpty();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('b'))).isEmpty();

			// 경계값(== now)과 미래값(> now)은 유지
			em.clear();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('c'))).isPresent();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('d'))).isPresent();
		}

		@Test
		@Transactional
		@DisplayName("경계값(== now)은 삭제되지 않는다")
		void boundary_not_deleted() {
			// given
			Instant base = Instant.now();
			Long memberId = 2L;

			save(memberId, hex('x'), base, base.plusSeconds(30)); // == now
			em.flush();
			em.clear();

			Instant nowForDelete = base.plusSeconds(30);

			// when
			int deleted = service.deleteExpiredUntil(nowForDelete);

			// then
			assertThat(deleted).isZero();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('x'))).isPresent();
		}

		@Test
		@Transactional
		@DisplayName("미래 만료(> now)는 삭제되지 않는다")
		void future_not_deleted() {
			// given
			Instant base = Instant.now();
			Long memberId = 3L;

			save(memberId, hex('y'), base, base.plusSeconds(999)); // > now
			em.flush();
			em.clear();

			Instant nowForDelete = base.plusSeconds(30);

			// when
			int deleted = service.deleteExpiredUntil(nowForDelete);

			// then
			assertThat(deleted).isZero();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('y'))).isPresent();
		}

		@Test
		@Transactional
		@DisplayName("삭제 대상이 없으면 0을 반환한다")
		void returns_zero_when_nothing_to_delete() {
			// given
			Instant base = Instant.now();
			Long memberId = 4L;

			save(memberId, hex('m'), base, base.plusSeconds(30));   // == now
			save(memberId, hex('n'), base, base.plusSeconds(31));   // > now
			em.flush();
			em.clear();

			Instant nowForDelete = base.plusSeconds(30);

			// when
			int deleted = service.deleteExpiredUntil(nowForDelete);

			// then
			assertThat(deleted).isZero();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('m'))).isPresent();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, hex('n'))).isPresent();
		}

		@Test
		@Transactional
		@DisplayName("여러 사용자의 만료 토큰을 한 번에 삭제하고, 삭제 건수를 정확히 반환한다")
		void deletes_for_multiple_members_and_counts_precisely() {
			// given
			Instant base = Instant.now();

			Long member1 = 10L;
			Long member2 = 11L;

			// member1: < now 2개, == now 1개
			save(member1, hex('a'), base, base.plusSeconds(5));   // < now
			save(member1, hex('b'), base, base.plusSeconds(10));  // < now
			save(member1, hex('c'), base, base.plusSeconds(30));  // == now

			// member2: > now 1개
			save(member2, hex('d'), base, base.plusSeconds(40));  // > now

			em.flush();
			em.clear();

			Instant nowForDelete = base.plusSeconds(30);

			// when
			int deleted = service.deleteExpiredUntil(nowForDelete);

			// then
			assertThat(deleted).isEqualTo(2); // a,b 만 삭제

			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(member1, hex('a'))).isEmpty();
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(member1, hex('b'))).isEmpty();

			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(member1, hex('c'))).isPresent(); // == now
			assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(member2, hex('d'))).isPresent(); // > now
		}
	}
}
