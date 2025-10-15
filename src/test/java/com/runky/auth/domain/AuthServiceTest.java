package com.runky.auth.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.auth.domain.token.jwt.JwtTokenProvider;
import com.runky.auth.domain.token.jwt.component.JwtTokenGenerator;
import com.runky.auth.domain.token.jwt.component.JwtTokenStore;
import com.runky.auth.domain.token.jwt.vo.JwtTokenPair;
import com.runky.auth.exception.domain.InvalidTokenException;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class JwtTokenProviderTest {

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	JwtTokenGenerator tokenGenerator; // 저장 없이 유효 RT 생성용(회전 예외 시나리오)

	@Autowired
	JwtTokenStore tokenStore;

	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Test
	@DisplayName("createTokenPair(): 새 RT를 저장하고, 클라이언트에는 AT/RT 원문을 반환한다")
	void create_success() {
		// given
		Long memberId1 = 1L;
		Long memberId2 = 2L;
		String role = "USER";

		// when
		JwtTokenPair pair1 = tokenProvider.createTokenPair(memberId1, role);
		JwtTokenPair pair2 = tokenProvider.createTokenPair(memberId2, role);

		// then
		assertThat(pair1.accessToken()).isNotBlank();
		assertThat(pair1.refreshToken()).isNotBlank();
		assertThat(pair2.accessToken()).isNotBlank();
		assertThat(pair2.refreshToken()).isNotBlank();

		// RT 저장 검증 (해시 저장이더라도 existsRefreshToken은 원문 RT로 확인)
		assertThat(tokenStore.existsRefreshToken(memberId1, pair1.refreshToken())).isTrue();
		assertThat(tokenStore.existsRefreshToken(memberId2, pair2.refreshToken())).isTrue();

		// 발급 타임스탬프 존재 검증
		assertThat(pair1.accessTokenIssuedAt()).isNotNull();
		assertThat(pair1.accessTokenExpiresAt()).isNotNull();
		assertThat(pair1.refreshTokenIssuedAt()).isNotNull();
		assertThat(pair1.refreshTokenExpiresAt()).isNotNull();
	}

	@Test
	@DisplayName("rotateTokens(): 제시된 RT 1건만 새 해시/만료로 교체하고 새 AT/RT를 반환한다")
	void rotate_success() {
		// given: 최초 발급(레코드 1건 생성)
		Long memberId = 10L;
		String role = "ADMIN";
		JwtTokenPair first = tokenProvider.createTokenPair(memberId, role);

		assertThat(tokenStore.existsRefreshToken(memberId, first.refreshToken())).isTrue();

		// when: 회전
		JwtTokenPair rotated = tokenProvider.rotateTokens(first.refreshToken());

		// then: 새 RT로 교체되고, 기존 RT는 더 이상 유효하지 않음
		assertThat(rotated.accessToken()).isNotBlank();
		assertThat(rotated.refreshToken()).isNotBlank();
		assertThat(rotated.refreshToken()).isNotEqualTo(first.refreshToken());

		assertThat(tokenStore.existsRefreshToken(memberId, first.refreshToken())).isFalse();
		assertThat(tokenStore.existsRefreshToken(memberId, rotated.refreshToken())).isTrue();

		// 타임스탬프 갱신 검증
		assertThat(rotated.refreshTokenIssuedAt()).isAfter(first.refreshTokenIssuedAt());
		assertThat(rotated.refreshTokenExpiresAt()).isAfter(first.refreshTokenExpiresAt());
	}

	@Test
	@DisplayName("rotateTokens(): DB에 해당 RT 레코드가 없으면 InvalidTokenException 발생")
	void rotate_missing_record_throws_invalid() {
		// given: 유효한 RT를 만들되, DB에는 저장하지 않음
		Long ghostMemberId = 77L;
		String role = "USER";
		JwtTokenPair generated = tokenGenerator.generateTokenPair(ghostMemberId, role);
		String rawRtNotSaved = generated.refreshToken();

		// 방어적 검증: 실제로 저장되어 있지 않아야 함
		assertThat(tokenStore.existsRefreshToken(ghostMemberId, rawRtNotSaved)).isFalse();

		// expect
		assertThatThrownBy(() -> tokenProvider.rotateTokens(rawRtNotSaved))
			.isInstanceOf(InvalidTokenException.class);
	}

	@Test
	@DisplayName("revokeTokens(): 해당 멤버의 모든 RT 삭제")
	void revoke_deletes_all_tokens_of_member() {
		// given: 동일 멤버에 RT 2개 누적(다중 RT 허용 시나리오)
		Long memberId = 99L;
		String role = "USER";
		JwtTokenPair p1 = tokenProvider.createTokenPair(memberId, role);
		JwtTokenPair p2 = tokenProvider.createTokenPair(memberId, role);

		assertThat(tokenStore.existsRefreshToken(memberId, p1.refreshToken())).isTrue();
		assertThat(tokenStore.existsRefreshToken(memberId, p2.refreshToken())).isTrue();

		// when
		tokenProvider.revokeTokens(memberId);

		// then
		assertThat(tokenStore.existsRefreshToken(memberId, p1.refreshToken())).isFalse();
		assertThat(tokenStore.existsRefreshToken(memberId, p2.refreshToken())).isFalse();
	}
}
