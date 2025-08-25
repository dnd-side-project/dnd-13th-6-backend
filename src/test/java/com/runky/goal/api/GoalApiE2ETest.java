package com.runky.goal.api;


import static org.assertj.core.api.Assertions.assertThat;

import com.runky.global.response.ApiResponse;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.Goal;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GoalApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private GoalRepository goalRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("GET /api/goals/me")
    class Get {
        private final String BASE_URL = "/api/goals/me";

        @Test
        @DisplayName("유저의 이번주 목표를 조회한다.")
        void getGoal() {
            MemberGoalSnapshot snapshot = goalRepository.save(
                    new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), false, LocalDate.of(2025, 8, 26)));

            ParameterizedTypeReference<ApiResponse<GoalResponse.Goal>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("X-USER-ID", "1");
            ResponseEntity<ApiResponse<GoalResponse.Goal>> response = testRestTemplate.exchange(BASE_URL,
                    HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            assertThat(response.getBody().getResult().goal()).isEqualTo(snapshot.getGoal().value());
        }
    }

    @Nested
    @DisplayName("GET /api/goals/crews/{crewId}")
    class GetCrewGoal {
        private final String BASE_URL = "/api/goals/crews/{crewId}";

        @Test
        @DisplayName("크루의 이번주 목표를 조회한다.")
        void getCrewGoal() {
            MemberGoalSnapshot memberGoalSnapshot = new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), false,
                    LocalDate.of(2025, 8, 26));
            CrewGoalSnapshot crewGoalSnapshot = goalRepository.save(CrewGoalSnapshot.of(
                    List.of(memberGoalSnapshot), 1L, LocalDate.of(2025, 8, 26)));

            ParameterizedTypeReference<ApiResponse<GoalResponse.Goal>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("X-USER-ID", "1");

            ResponseEntity<ApiResponse<GoalResponse.Goal>> response = testRestTemplate.exchange(BASE_URL,
                    HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType, 1L);

            assertThat(response.getBody().getResult().goal()).isEqualTo(crewGoalSnapshot.getGoal().value());
        }
    }
}
