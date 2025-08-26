package com.runky.reward.api;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.runky.auth.domain.port.TokenIssuer;
import com.runky.global.response.ApiResponse;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.BadgeRepository;
import com.runky.reward.domain.Clover;
import com.runky.reward.domain.CloverRepository;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RewardApiE2ETest {

	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;
	@Autowired
	private BadgeRepository badgeRepository;
	@Autowired
	private CloverRepository cloverRepository;

	@Autowired
	private TokenIssuer tokenIssuer;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	private HttpHeaders authHeaders(long memberId, String role) {
		var issued = tokenIssuer.issue(memberId, role);
		String accessToken = issued.access().token();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.COOKIE, "accessToken=" + accessToken);
		return headers;
	}

	@Nested
	@DisplayName("GET api/rewards/badges")
	class GetBadges {
		final String BASE_URL = "/api/rewards/badges";

		@Test
		@DisplayName("유저의 배지 목록을 조회한다.")
		void getBadges() {
			Badge badge1 = badgeRepository.save(Badge.of("badge1.pvg", "배지1"));
			badgeRepository.save(badge1.issue(1L));
			ParameterizedTypeReference<ApiResponse<RewardResponse.Images>> responseType = new ParameterizedTypeReference<>() {
			};

			HttpHeaders httpHeaders = authHeaders(1L, "USER");

			ResponseEntity<ApiResponse<RewardResponse.Images>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getBody().getResult().badges()).hasSize(1);
			assertThat(response.getBody().getResult().badges().get(0).badge()).isEqualTo(badge1.getImageUrl());
		}
	}

	@Nested
	@DisplayName("GET api/rewards/clovers")
	class GetCloverCount {
		final String BASE_URL = "/api/rewards/clovers";

		@Test
		@DisplayName("유저의 클로버 개수를 조회한다.")
		void getCloverCount() {
			Clover clover = Clover.of(1L);
			clover.add(1000L);
			cloverRepository.save(clover);

			HttpHeaders httpHeaders = authHeaders(1L, "USER");

			ResponseEntity<ApiResponse<RewardResponse.Clover>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders),
					new ParameterizedTypeReference<>() {
					});

			assertThat(response.getBody().getResult().count()).isEqualTo(1000L);
		}
	}
}
