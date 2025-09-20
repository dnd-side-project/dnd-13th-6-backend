package com.runky.auth.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.auth.domain.port.RefreshTokenRepository;
import com.runky.auth.domain.port.TokenHasher;
import com.runky.auth.domain.port.TokenIssuer;
import com.runky.auth.exception.domain.TokenMismatchException;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class AuthTokenServiceTest {
	@Autowired
	AuthTokenService service;

	@Autowired
	TokenIssuer tokenIssuer;

	@Autowired
	TokenHasher tokenHasher;

	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Test
	@DisplayName("issue(): 새 RT를 해시로 저장하고, 클라이언트에는 원문 AT/RT를 반환한다")
	void issue_success() {
		// given
		Long memberId = 1L;
		Long memberId2 = 2L;

		String role = "USER";

		// when
		AuthInfo.TokenPair pair = service.issue(memberId, role);
		System.out.println(pair.accessToken());
		AuthInfo.TokenPair pair2 = service.issue(memberId2, role);
		System.out.println(pair2.accessToken());

		// then
		assertThat(pair.accessToken()).isNotBlank();
		assertThat(pair.refreshToken()).isNotBlank();

		String expectedHash = tokenHasher.hash(pair.refreshToken());
		Optional<RefreshToken> saved = refreshTokenRepository
			.findByMemberIdAndTokenHash(memberId, expectedHash);

		assertThat(saved).isPresent();
		assertThat(saved.get().getMemberId()).isEqualTo(memberId);
		assertThat(saved.get().getTokenHash()).isEqualTo(expectedHash);
	}

	@Test
	@DisplayName("rotate(): 제시된 RT 1건만 새 해시/만료로 갱신하고 새 AT/RT를 반환한다")
	void rotate_success() {
		// given: 최초 발급(레코드 1건 생성)
		Long memberId = 10L;
		String role = "ADMIN";
		AuthInfo.TokenPair first = service.issue(memberId, role);

		String oldHash = tokenHasher.hash(first.refreshToken());
		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, oldHash)).isPresent();

		// when: 회전
		AuthInfo.TokenPair rotated = service.rotateByRefreshToken(first.refreshToken());

		// then: 새 RT로 교체되었고, 기존 해시는 사라짐
		String newHash = tokenHasher.hash(rotated.refreshToken());

		assertThat(rotated.accessToken()).isNotBlank();
		assertThat(rotated.refreshToken()).isNotEqualTo(first.refreshToken());
		assertThat(newHash).isNotEqualTo(oldHash);

		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, oldHash)).isEmpty();
		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, newHash)).isPresent();
	}

	@Test
	@DisplayName("rotate(): DB에 해당 RT 레코드가 없으면 ExpiredTokenException 발생")
	void rotate_missing_record_throws_expired() {
		// given: 유효한 RT를 만들되, DB에는 저장하지 않음
		Long ghostMemberId = 77L;
		String role = "USER";
		var issued = tokenIssuer.issue(ghostMemberId, role);
		String rawRtNotSaved = issued.refresh().token();

		// expect
		assertThatThrownBy(() -> service.rotateByRefreshToken(rawRtNotSaved))
			.isInstanceOf(TokenMismatchException.class);
	}

	@Test
	@DisplayName("logoutByMemberId(): 해당 멤버의 모든 RT 삭제")
	void logout_deletes_all_tokens_of_member() {
		// given: 동일 멤버에 RT 두 개 누적(다중 RT 허용 시나리오)
		Long memberId = 99L;
		String role = "USER";
		AuthInfo.TokenPair p1 = service.issue(memberId, role);
		AuthInfo.TokenPair p2 = service.issue(memberId, role);

		String h1 = tokenHasher.hash(p1.refreshToken());
		String h2 = tokenHasher.hash(p2.refreshToken());
		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, h1)).isPresent();
		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, h2)).isPresent();

		// when
		service.delete(memberId);

		// then
		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, h1)).isEmpty();
		assertThat(refreshTokenRepository.findByMemberIdAndTokenHash(memberId, h2)).isEmpty();
	}
}
