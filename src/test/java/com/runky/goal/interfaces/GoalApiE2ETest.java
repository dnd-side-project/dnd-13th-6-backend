package com.runky.goal.interfaces;

import static org.assertj.core.api.Assertions.*;

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

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
import com.runky.global.response.ApiResponse;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.Goal;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import com.runky.utils.DatabaseCleanUp;
import com.runky.utils.TestTokenIssuer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GoalApiE2ETest {

	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;
	@Autowired
	private GoalRepository goalRepository;
	@Autowired
	private CrewRepository crewRepository;
	@Autowired
	private TestTokenIssuer tokenIssuer;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Nested
	@DisplayName("PATCH /api/goals/me")
	class Update {
		private final String BASE_URL = "/api/goals/me";

		@Test
		@DisplayName("유저의 이번주 목표를 수정한다.")
		void updateGoal() {
			goalRepository.save(MemberGoal.from(1L));

			ParameterizedTypeReference<ApiResponse<GoalResponse.Goal>> responseType = new ParameterizedTypeReference<>() {
			};
			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
			GoalRequest.Goal request = new GoalRequest.Goal(BigDecimal.TEN);

			ResponseEntity<ApiResponse<GoalResponse.Goal>> response = testRestTemplate.exchange(BASE_URL,
				HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders), responseType);

			MemberGoal memberGoal = goalRepository.findMemberGoalByMemberId(1L).orElseThrow();
			assertThat(response.getBody().getResult().goal()).isEqualTo(new BigDecimal("10.00"));
			assertThat(memberGoal.getGoal().value()).isEqualTo(new BigDecimal("10.00"));
		}
	}

	@Nested
	@DisplayName("GET /api/goals/me")
	class GetDeviceToken {
		private final String BASE_URL = "/api/goals/me";

		@Test
		@DisplayName("유저의 이번주 목표를 조회한다.")
		void getGoal() {
			LocalDate todayKst = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
			MemberGoalSnapshot snapshot = goalRepository.save(
				new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), false, todayKst));
			ParameterizedTypeReference<ApiResponse<GoalResponse.Goal>> responseType = new ParameterizedTypeReference<>() {
			};
			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
			ResponseEntity<ApiResponse<GoalResponse.Goal>> response = testRestTemplate.exchange(BASE_URL,
				HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getBody().getResult().goal()).isEqualTo(snapshot.getGoal().value());
		}
	}

	@Nested
	@DisplayName("GET /api/goals/crews/{crewId}")
	class GetDeviceTokenCrewGoal {
		private final String BASE_URL = "/api/goals/crews/{crewId}";

		@Test
		@DisplayName("크루의 이번주 목표를 조회한다.")
		void getCrewGoal() {
			LocalDate todayKst = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
			MemberGoalSnapshot memberGoalSnapshot =
				new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), false, todayKst);
			CrewGoalSnapshot crewGoalSnapshot =
				goalRepository.save(CrewGoalSnapshot.of(List.of(memberGoalSnapshot), 1L, todayKst));
			ParameterizedTypeReference<ApiResponse<GoalResponse.Goal>> responseType = new ParameterizedTypeReference<>() {
			};
			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");

			ResponseEntity<ApiResponse<GoalResponse.Goal>> response = testRestTemplate.exchange(BASE_URL,
				HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType, 1L);

			assertThat(response.getBody().getResult().goal()).isEqualTo(crewGoalSnapshot.getGoal().value());
		}
	}

	@Nested
	@DisplayName("GET /api/goals/me/last/achieve")
	class GetDeviceTokenAchieve {
		private final String BASE_URL = "/api/goals/me/last/achieve";

		@Test
		@DisplayName("유저의 이번주 목표 달성 여부를 조회한다.")
		void getAchieve() {
			goalRepository.save(
				new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), true, LocalDate.now().minusWeeks(1)));

			ParameterizedTypeReference<ApiResponse<GoalResponse.Achieve>> responseType = new ParameterizedTypeReference<>() {
			};

			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
			ResponseEntity<ApiResponse<GoalResponse.Achieve>> response = testRestTemplate.exchange(BASE_URL,
				HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getBody().getResult().achieved()).isTrue();
		}
	}

	@Nested
	@DisplayName("GET /api/goals/crews/{crewId}/last/achieve")
	class GetDeviceTokenCrewAchieve {
		private final String BASE_URL = "/api/goals/crews/{crewId}/last/achieve";

		@Test
		@DisplayName("크루의 이번주 목표 달성 여부를 조회한다.")
		void getCrewAchieve() {
			CrewGoalSnapshot crewGoalSnapshot = new CrewGoalSnapshot(1L, new Goal(BigDecimal.TEN), true,
				WeekUnit.from(LocalDate.now().minusWeeks(1)));
			goalRepository.save(crewGoalSnapshot);

			ParameterizedTypeReference<ApiResponse<GoalResponse.Achieve>> responseType = new ParameterizedTypeReference<>() {
			};
			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
			String url = "/api/goals/crews/1/last/achieve";

			ResponseEntity<ApiResponse<GoalResponse.Achieve>> response = testRestTemplate.exchange(url,
				HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType, 1L);

			assertThat(response.getBody().getResult().achieved()).isTrue();
		}
	}

	@Nested
	@DisplayName("GET /api/goals/me/last/clovers")
	class GetDeviceTokenMemberGoalClovers {
		private final String BASE_URL = "/api/goals/me/last/clovers";

		@Test
		@DisplayName("유저의 지난주 클로버 개수를 조회한다.")
		void getMemberGoalClovers() {
			MemberGoalSnapshot memberGoalSnapshot = new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), true,
				LocalDate.now().minusWeeks(1));
			goalRepository.save(memberGoalSnapshot);

			ParameterizedTypeReference<ApiResponse<GoalResponse.Clover>> responseType = new ParameterizedTypeReference<>() {
			};

			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
			ResponseEntity<ApiResponse<GoalResponse.Clover>> response = testRestTemplate.exchange(BASE_URL,
				HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getBody().getResult().count()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("GET /api/goals/crews/last/clovers")
	class GetDeviceTokenCrewGoalClovers {
		private final String BASE_URL = "/api/goals/crews/last/clovers";

		@Test
		@DisplayName("크루의 지난주 클로버 개수를 조회한다.")
		void getCrewGoalClovers() {
			Crew crew1 = crewRepository.save(Crew.of(new CrewCommand.Create(1L, "name1"), new Code("abc123")));
			Crew crew2 = crewRepository.save(Crew.of(new CrewCommand.Create(1L, "name2"), new Code("abc123")));
			Crew crew3 = crewRepository.save(Crew.of(new CrewCommand.Create(1L, "name3"), new Code("abc123")));
			CrewGoalSnapshot snapshot1 = CrewGoalSnapshot.empty(crew1.getId(), LocalDate.now().minusWeeks(1));
			snapshot1.achieve();
			goalRepository.save(snapshot1);
			CrewGoalSnapshot snapshot2 = CrewGoalSnapshot.empty(crew2.getId(), LocalDate.now().minusWeeks(1));
			snapshot2.achieve();
			goalRepository.save(snapshot2);
			CrewGoalSnapshot snapshot3 = CrewGoalSnapshot.empty(crew3.getId(), LocalDate.now().minusWeeks(1));
			goalRepository.save(snapshot3);

			ParameterizedTypeReference<ApiResponse<GoalResponse.Clover>> responseType = new ParameterizedTypeReference<>() {
			};

			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");
			ResponseEntity<ApiResponse<GoalResponse.Clover>> response = testRestTemplate.exchange(BASE_URL,
				HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getBody().getResult().count()).isEqualTo(6);
		}
	}
}
