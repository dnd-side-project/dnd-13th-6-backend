package com.runky.running.interfaces.api;

import com.runky.global.response.ApiResponse;
import com.runky.running.domain.Running;
import com.runky.running.infra.jpa.RunningJpaRepository;
import com.runky.utils.DatabaseCleanUp;
import com.runky.utils.TestTokenIssuer;
import org.apache.http.client.utils.URIBuilder;
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

import java.net.URISyntaxException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalendarApiE2ETest {
    @Autowired
    private RunningJpaRepository runningJpaRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private TestTokenIssuer tokenIssuer;
    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("GET /api/calendar/weekly")
    class GetWeekly {
        private final String BASE_URL = "/api/calendar/weekly";

        @Test
        @DisplayName("주간 러닝 기록을 조회한다.")
        void getWeeklyHistories() throws URISyntaxException {
            LocalDateTime now = LocalDateTime.of(2025, 10, 6, 10, 0);
            for (int i = 0; i < 7; i++) {
                Running running = Running.start(1L, now.plusDays(i));
                running.finish(5.0, 60 * 30, 6.0, running.getStartedAt().plusMinutes(30));
                runningJpaRepository.save(running);
            }
            LocalDateTime end = now.toLocalDate().plusDays(7).atStartOfDay();
            Running excluded = Running.start(1L, end.minusMinutes(30));
            excluded.finish(5.0, 60 * 30, 6.0, end);
            runningJpaRepository.save(excluded);

            HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
            String url = new URIBuilder(BASE_URL)
                    .addParameter("date", "2025-10-06")
                    .build()
                    .toString();
            ParameterizedTypeReference<ApiResponse<CalendarResponse.Histories>> type = new ParameterizedTypeReference<>() {
            };

            ResponseEntity<ApiResponse<CalendarResponse.Histories>> response
                    = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), type);

            assertThat(response.getBody().getResult().totalDistance()).isEqualTo(35.0);
            assertThat(response.getBody().getResult().totalDuration()).isEqualTo(60 * 30 * 7);
            assertThat(response.getBody().getResult().histories()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("GET /api/calendar/monthly")
    class GetMonthly {
        private final String BASE_URL = "/api/calendar/monthly";

        @Test
        @DisplayName("월간 러닝 기록을 조회한다.")
        void getMonthlyHistories() throws URISyntaxException {
            LocalDateTime now = LocalDateTime.of(2025, 10, 1, 10, 0);
            for (int i = 0; i < 4; i++) {
                Running running = Running.start(1L, now.plusWeeks(i));
                running.finish(5.0, 60 * 30, 6.0, running.getStartedAt().plusMinutes(30));
                runningJpaRepository.save(running);
            }
            Running excluded = Running.start(1L, LocalDateTime.of(2025, 10, 31, 23, 30));
            excluded.finish(5.0, 60 * 30, 6.0, excluded.getStartedAt().plusMinutes(30));
            runningJpaRepository.save(excluded);


            HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
            String url = new URIBuilder(BASE_URL)
                    .addParameter("year", "2025")
                    .addParameter("month", "10")
                    .build()
                    .toString();
            ParameterizedTypeReference<ApiResponse<CalendarResponse.Histories>> type = new ParameterizedTypeReference<>() {
            };

            ResponseEntity<ApiResponse<CalendarResponse.Histories>> response
                    = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), type);

            assertThat(response.getBody().getResult().totalDistance()).isEqualTo(20.0);
            assertThat(response.getBody().getResult().totalDuration()).isEqualTo(60 * 30 * 4);
            assertThat(response.getBody().getResult().histories()).hasSize(4);
        }
    }
}
