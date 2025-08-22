package com.runky.crew.api;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewMemberCount;
import com.runky.crew.domain.CrewRepository;
import com.runky.global.response.ApiResponse;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrewApiE2ETest {
	private final TestRestTemplate testRestTemplate;
	private final DatabaseCleanUp databaseCleanUp;
	private final CrewRepository crewRepository;

	@Autowired
	public CrewApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp,
		CrewRepository crewRepository) {
		this.testRestTemplate = testRestTemplate;
		this.databaseCleanUp = databaseCleanUp;
		this.crewRepository = crewRepository;
	}

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Nested
	@DisplayName("POST /api/crews")
	class Create {
		final String BASE_URL = "/api/crews";

		@Test
		@DisplayName("크루를 생성한다.")
		void createCrew() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Create>> responseType = new ParameterizedTypeReference<>() {
			};
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			CrewRequest.Create request = new CrewRequest.Create("Test Crew");

			ResponseEntity<ApiResponse<CrewResponse.Create>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(request, httpHeaders),
					responseType);

			Crew crew = crewRepository.findById(response.getBody().getResult().crewId()).orElseThrow();
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(crew.getId());
			assertThat(response.getBody().getResult().name()).isEqualTo(crew.getName());
			assertThat(response.getBody().getResult().code()).isEqualTo(crew.getCode().value());
		}
	}

	@Nested
	@DisplayName("POST /api/crews/join")
	class Join {
		final String BASE_URL = "/api/crews/join";

		@Test
		@DisplayName("크루에 참여한다.")
		void joinCrew() {
			Crew crew = Crew.of(new CrewCommand.Create(1L, "Crew"), new Code("abc123"));
			Crew savedCrew = crewRepository.save(crew);
			Long userId = 2L;
			crewRepository.save(CrewMemberCount.of(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Join>> responseType = new ParameterizedTypeReference<>() {
			};
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			CrewRequest.Join request = new CrewRequest.Join(crew.getCode().value());

			ResponseEntity<ApiResponse<CrewResponse.Join>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(request, httpHeaders),
					responseType);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(savedCrew.getId());
		}
	}

	@Nested
	@DisplayName("GET /api/crews")
	class FindCrews {
		final String BASE_URL = "/api/crews";

		@Test
		@DisplayName("사용자의 크루 목록을 조회한다.")
		void getCrews() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew1 = crewRepository.save(Crew.of(new CrewCommand.Create(userId, "Crew 1"), new Code("abc123")));
			Crew crew2 = crewRepository.save(Crew.of(new CrewCommand.Create(userId, "Crew 2"), new Code("abc456")));

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Cards>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Cards>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crews()).hasSize(2);
			assertThat(response.getBody().getResult().crews())
				.extracting("crewId")
				.containsExactlyInAnyOrder(crew1.getId(), crew2.getId());
		}
	}

	@Nested
	@DisplayName("GET /api/crews/{crewId}")
	class FindCrew {
		final String BASE_URL = "/api/crews/{crewId}";

		@Test
		@DisplayName("크루의 상세 정보를 조회한다.")
		void getCrew() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew = crewRepository.save(Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123")));

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Detail>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Detail>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType,
					crew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(crew.getId());
			assertThat(response.getBody().getResult().name()).isEqualTo(crew.getName());
			assertThat(response.getBody().getResult().code()).isEqualTo(crew.getCode().value());
			assertThat(response.getBody().getResult().memberCount()).isEqualTo(crew.getActiveMemberCount());
			assertThat(response.getBody().getResult().notice()).isEqualTo(crew.getNotice());
		}
	}

	@Nested
	@DisplayName("DELETE /api/crews/{crewId}/members/me")
	class Leave {
		final String BASE_URL = "/api/crews/{crewId}/members/me";

		@Test
		@DisplayName("크루에 속한 사용자가 크루를 탈퇴한다.")
		void leaveCrew() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);
			CrewMemberCount crewMemberCount = CrewMemberCount.of(2L);
			crewMemberCount.increment();
			crewRepository.save(crewMemberCount);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(2L));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Leave>> responseType = new ParameterizedTypeReference<>() {
			};

			CrewRequest.Leave request = new CrewRequest.Leave(null);
			ResponseEntity<ApiResponse<CrewResponse.Leave>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE, new HttpEntity<>(request, httpHeaders),
					responseType,
					crew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(crew.getId());
		}
	}

	@Nested
	@DisplayName("GET /api/crews/{crewId}/members")
	class FindCrewMembers {
		final String BASE_URL = "/api/crews/{crewId}/members";

		@Test
		@DisplayName("크루의 멤버 목록을 조회한다.")
		void getCrewMembers() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			crew.joinMember(3L);
			crew.joinMember(4L);
			crew.leaveMember(4L);
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Members>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Members>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType,
					savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().members()).hasSize(3);
		}
	}

	@Nested
	@DisplayName("PATCH /api/crews/{crewId}/notice")
	class UpdateNotice {
		final String BASE_URL = "/api/crews/{crewId}/notice";

		@Test
		@DisplayName("크루의 공지사항을 업데이트한다.")
		void updateNotice() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Notice>> responseType = new ParameterizedTypeReference<>() {
			};

			CrewRequest.Notice request = new CrewRequest.Notice("New Notice");
			ResponseEntity<ApiResponse<CrewResponse.Notice>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders),
					responseType, savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().notice()).isEqualTo("New Notice");
		}
	}

	@Nested
	@DisplayName("PATCH /api/crews/{crewId}/name")
	class UpdateCrewName {
		final String BASE_URL = "/api/crews/{crewId}/name";

		@Test
		@DisplayName("크루의 이름을 업데이트한다.")
		void updateCrewName() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Old Crew Name"), new Code("abc123"));
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Name>> responseType = new ParameterizedTypeReference<>() {
			};

			CrewRequest.Name request = new CrewRequest.Name("New Crew Name");
			ResponseEntity<ApiResponse<CrewResponse.Name>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders),
					responseType, savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().name()).isEqualTo("New Crew Name");
		}
	}

	@Nested
	@DisplayName("DELETE /api/crews/{crewId}")
	class DeleteCrew {
		final String BASE_URL = "/api/crews/{crewId}";

		@Test
		@DisplayName("크루를 삭제한다.")
		void deleteCrew() {
			long userId = 1L;
			CrewMemberCount count1 = CrewMemberCount.of(1L);
			count1.increment();
			CrewMemberCount count2 = CrewMemberCount.of(2L);
			count2.increment();
			crewRepository.save(count1);
			crewRepository.save(count2);
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Disband>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Disband>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE, new HttpEntity<>(httpHeaders), responseType,
					savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().name()).isEqualTo(savedCrew.getName());
		}
	}

	@Nested
	@DisplayName("PATCH /api/crews/{crewId}/leader")
	class UpdateLeader {
		final String BASE_URL = "/api/crews/{crewId}/leader";

		@Test
		@DisplayName("크루의 리더를 변경한다.")
		void updateLeader() {
			long userId = 1L;
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(userId));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Delegate>> responseType = new ParameterizedTypeReference<>() {
			};

			CrewRequest.Delegate request = new CrewRequest.Delegate(2L);
			ResponseEntity<ApiResponse<CrewResponse.Delegate>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders),
					responseType, savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().leaderId()).isEqualTo(2L);
		}
	}

	@Nested
	@DisplayName("DELETE /api/crews/{crewId}/members/{memberId}")
	class BanMember {
		final String BASE_URL = "/api/crews/{crewId}/members/{memberId}";

		@Test
		@DisplayName("크루에서 멤버를 추방한다.")
		void banMember() {
			long userId = 1L;
			CrewMemberCount count1 = CrewMemberCount.of(1L);
			count1.increment();
			CrewMemberCount count2 = CrewMemberCount.of(2L);
			count2.increment();
			crewRepository.save(count1);
			crewRepository.save(count2);
			Crew crew = Crew.of(new CrewCommand.Create(1L, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-USER-ID", String.valueOf(1L));
			ParameterizedTypeReference<ApiResponse<CrewResponse.Ban>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Ban>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE, new HttpEntity<>(httpHeaders), responseType,
					savedCrew.getId(), 2L);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().targetId()).isEqualTo(2L);
		}
	}
}
