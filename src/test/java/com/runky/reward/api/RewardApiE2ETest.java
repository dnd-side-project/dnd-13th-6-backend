package com.runky.reward.api;

import static org.assertj.core.api.Assertions.*;

import com.runky.utils.TestTokenIssuer;
import java.util.List;
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
    private TestTokenIssuer testTokenIssuer;
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
    @DisplayName("PATCH api/rewards/gotcha")
    class Gotcha {
        final String BASE_URL = "/api/rewards/gotcha";

        @Test
        @DisplayName("유저가 뽑기한 배지를 조회한다.")
        void getGotchaBadge() {
            badgeRepository.save(badgeRepository.save(Badge.of("badge1.pvg", "뱃지 1")));
            badgeRepository.save(badgeRepository.save(Badge.of("badge2.pvg", "뱃지 2")));
            badgeRepository.save(badgeRepository.save(Badge.of("badge3.pvg", "뱃지 3")));
            badgeRepository.save(badgeRepository.save(Badge.of("badge4.pvg", "뱃지 4")));
            badgeRepository.save(badgeRepository.save(Badge.of("badge5.pvg", "뱃지 5")));
            Clover clover = Clover.of(1L);
            clover.add(100L);
            cloverRepository.save(clover);
            ParameterizedTypeReference<ApiResponse<RewardResponse.Gotcha>> responseType = new ParameterizedTypeReference<>() {
            };

            testTokenIssuer.issue(1L, "USER");
            HttpHeaders httpHeaders = authHeaders(1L, "USER");

            ResponseEntity<ApiResponse<RewardResponse.Gotcha>> response =
                testRestTemplate.exchange(BASE_URL, HttpMethod.PATCH, new HttpEntity<>(httpHeaders), responseType);

            List<Badge> badges = badgeRepository.findBadgesOf(1L);
            assertThat(badges).hasSize(1);
            assertThat(response.getBody().getResult().id()).isEqualTo(badges.get(0).getId());
            assertThat(response.getBody().getResult().imageUrl()).isEqualTo(badges.get(0).getImageUrl());
            assertThat(response.getBody().getResult().name()).isEqualTo(badges.get(0).getName());
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
