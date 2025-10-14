package com.runky.crew.interfaces;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import com.runky.crew.interfaces.api.CrewRequest;
import com.runky.crew.interfaces.api.CrewResponse;
import com.runky.global.response.ApiResponse;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.Goal;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.WeekUnit;
import com.runky.member.domain.ExternalAccount;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberRepository;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.BadgeRepository;
import com.runky.running.domain.Running;
import com.runky.running.domain.RunningRepository;
import com.runky.utils.DatabaseCleanUp;
import com.runky.utils.TestTokenIssuer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrewApiE2ETest {
	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;
	@Autowired
	private CrewRepository crewRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private BadgeRepository badgeRepository;
	@Autowired
	private RunningRepository runningRepository;
	@Autowired
	private GoalRepository goalRepository;
	@Autowired
	private TestTokenIssuer tokenIssuer;

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
			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

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
			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

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
			Member saveMember1 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id1"), "name1"));
			Member member2 = Member.register(ExternalAccount.of("kakao", "id2"), "name2");
			member2.changeBadge(2L);
			Member saveMember2 = memberRepository.save(member2);
			Member member3 = Member.register(ExternalAccount.of("kakao", "id3"), "name3");
			member3.changeBadge(3L);
			Member saveMember3 = memberRepository.save(member3);
			Member member4 = Member.register(ExternalAccount.of("kakao", "id4"), "name4");
			member4.changeBadge(4L);
			Member saveMember4 = memberRepository.save(member4);
			Running running = Running.builder()
				.runnerId(saveMember1.getId())
				.status(Running.Status.ENDED)
				.startedAt(LocalDateTime.now().minusHours(1))
				.endedAt(LocalDateTime.now())
				.totalDistanceMeter(10000.0)
				.durationSeconds(3600L)
				.avgSpeedMPS(2.5)
				.build();
			Running running1 = Running.builder()
				.runnerId(saveMember3.getId())
				.status(Running.Status.ENDED)
				.startedAt(LocalDateTime.now().minusHours(1))
				.endedAt(LocalDateTime.now())
				.totalDistanceMeter(10000.0)
				.durationSeconds(3600L)
				.avgSpeedMPS(2.5)
				.build();
			runningRepository.save(running);
			runningRepository.save(running1);

			Crew crew1 = Crew.of(new CrewCommand.Create(saveMember1.getId(), "crew1"), new Code("abc123"));
			crew1.joinMember(saveMember2.getId());
			crewRepository.save(crew1);
			CrewGoalSnapshot snapshot1 = goalRepository.save(
				new CrewGoalSnapshot(crew1.getId(), new Goal(new BigDecimal("10.5")), false,
					WeekUnit.from(LocalDate.now().minusWeeks(1))));
			Crew crew2 = Crew.of(new CrewCommand.Create(saveMember2.getId(), "crew2"), new Code("abc124"));
			crew2.joinMember(saveMember1.getId());
			crew2.joinMember(saveMember3.getId());
			crew2.joinMember(saveMember4.getId());
			crew2.leaveMember(saveMember4.getId());
			crewRepository.save(crew2);
			CrewGoalSnapshot snapshot2 = goalRepository.save(
				new CrewGoalSnapshot(crew2.getId(), new Goal(new BigDecimal("10.5")), false,
					WeekUnit.from(LocalDate.now().minusWeeks(1))));

			Running running2 = Running.builder()
				.runnerId(saveMember3.getId())
				.status(Running.Status.RUNNING)
				.startedAt(LocalDateTime.now().minusHours(1))
				.build();
			runningRepository.save(running2);

			badgeRepository.save(Badge.of("/badge1.png", "badge 1"));
			badgeRepository.save(Badge.of("/badge2.png", "badge 2"));
			badgeRepository.save(Badge.of("/badge3.png", "badge 3"));
			badgeRepository.save(Badge.of("/badge4.png", "badge 4"));

			HttpHeaders httpHeaders = tokenIssuer.issue(saveMember1.getId(), "USER");

			ParameterizedTypeReference<ApiResponse<CrewResponse.Cards>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Cards>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crews()).hasSize(2);
			assertThat(response.getBody().getResult().crews())
				.extracting("crewId")
				.containsExactlyInAnyOrder(crew1.getId(), crew2.getId());
			assertThat(response.getBody().getResult().crews())
				.extracting("name")
				.containsExactlyInAnyOrder(crew1.getName(), crew2.getName());
			assertThat(response.getBody().getResult().crews())
				.extracting("goal")
				.containsExactlyInAnyOrder(new BigDecimal("10.50"), new BigDecimal("10.50"));
			assertThat(response.getBody().getResult().crews())
				.extracting("runningDistance")
				.containsExactlyInAnyOrder(10000.0, 20000.0);
		}
	}

	@Nested
	@DisplayName("GET /api/crews/{crewId}")
	class FindCrew {
		final String BASE_URL = "/api/crews/{crewId}";

		@Test
		@DisplayName("크루의 상세 정보를 조회한다.")
		void getCrew() {
			Member leader = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id1"), "name1"));
			Crew crew = Crew.of(new CrewCommand.Create(leader.getId(), "crew 1"), new Code("abc123"));
			crew.joinMember(2L);
			crew.joinMember(3L);
			crew.joinMember(4L);
			crewRepository.save(crew);

			CrewGoalSnapshot snapshot = new CrewGoalSnapshot(crew.getId(), new Goal(new BigDecimal("10.5")), false,
				WeekUnit.from(LocalDate.now().minusWeeks(1)));
			goalRepository.save(snapshot);

			Running running1 = Running.builder()
				.runnerId(leader.getId())
				.status(Running.Status.ENDED)
				.startedAt(LocalDateTime.now().minusHours(1))
				.endedAt(LocalDateTime.now())
				.totalDistanceMeter(10000.0)
				.durationSeconds(3600L)
				.avgSpeedMPS(2.5)
				.build();
			runningRepository.save(running1);

			HttpHeaders httpHeaders = tokenIssuer.issue(leader.getId(), "USER");
			ParameterizedTypeReference<ApiResponse<CrewResponse.Detail>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Detail>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType,
					crew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(crew.getId());
			assertThat(response.getBody().getResult().name()).isEqualTo(crew.getName());
			assertThat(response.getBody().getResult().code()).isEqualTo(crew.getCode().value());
			assertThat(response.getBody().getResult().leaderNickname()).isEqualTo(leader.getNickname().value());
			assertThat(response.getBody().getResult().memberCount()).isEqualTo(crew.getActiveMemberCount());
			assertThat(response.getBody().getResult().notice()).isEqualTo(crew.getNotice());
			assertThat(response.getBody().getResult().goal()).isEqualTo(new BigDecimal("10.50"));
			assertThat(response.getBody().getResult().runningDistance()).isEqualTo(10000.0);
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

			HttpHeaders httpHeaders = tokenIssuer.issue(2L, "USER");

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

		@Test
		@DisplayName("리더가 아닐 경우, RequestBody를 넣지 않더라도, 크루를 탈퇴할 수 있다.")
		void leaveCrew_withoutNewLeader() {
			long userId = 1L;
			crewRepository.save(CrewMemberCount.of(userId));
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);
			CrewMemberCount crewMemberCount = CrewMemberCount.of(2L);
			crewMemberCount.increment();
			crewRepository.save(crewMemberCount);

			HttpHeaders httpHeaders = tokenIssuer.issue(2L, "USER");

			ParameterizedTypeReference<ApiResponse<CrewResponse.Leave>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Leave>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE, new HttpEntity<>(httpHeaders),
					responseType,
					savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(savedCrew.getId());
		}

		@Test
		@DisplayName("리더가 크루를 탈퇴할 경우, RequestBody에 새로운 리더의 ID를 반드시 넣어야 한다.")
		void leaveCrew_leaderMustSetNewLeader() {
			long userId = 1L;
			CrewMemberCount crewMemberCount = CrewMemberCount.of(userId);
			crewMemberCount.increment();
			crewRepository.save(crewMemberCount);
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = tokenIssuer.issue(1L, "USER");

			ParameterizedTypeReference<ApiResponse<CrewResponse.Leave>> responseType = new ParameterizedTypeReference<>() {
			};

			CrewRequest.Leave request = new CrewRequest.Leave(2L);
			ResponseEntity<ApiResponse<CrewResponse.Leave>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE, new HttpEntity<>(request, httpHeaders),
					responseType,
					savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().crewId()).isEqualTo(savedCrew.getId());
		}
	}

	@Nested
	@DisplayName("GET /api/crews/{crewId}/members")
	class FindCrewMembers {
		final String BASE_URL = "/api/crews/{crewId}/members";

		@Test
		@DisplayName("크루의 멤버 목록을 조회한다.")
		void getCrewMembers() {
			Member leader = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id1"), "name1"));
			Member member2 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id2"), "name2"));
			Member member3 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id3"), "name3"));
			Badge badge1 = badgeRepository.save(Badge.of("/badge1.png", "badge 1"));
			leader.changeBadge(badge1.getId());
			member2.changeBadge(badge1.getId());
			member3.changeBadge(badge1.getId());
			memberRepository.save(leader);
			memberRepository.save(member2);
			memberRepository.save(member3);

			Crew crew = Crew.of(new CrewCommand.Create(leader.getId(), "crew 1"), new Code("abc123"));
			crew.joinMember(member2.getId());
			crew.joinMember(member3.getId());
			Crew savedCrew = crewRepository.save(crew);

			LocalDateTime now = LocalDateTime.now();

			runningRepository.save(Running.builder()
				.runnerId(member2.getId())
				.status(Running.Status.ENDED)
				.startedAt(now.minusHours(1))
				.endedAt(now)
				.totalDistanceMeter(10000.0)
				.durationSeconds(3600L)
				.avgSpeedMPS(2.5)
				.build());
			runningRepository.save(Running.builder()
				.runnerId(member2.getId())
				.status(Running.Status.RUNNING)
				.startedAt(now.minusHours(1))
				.build());

			runningRepository.save(Running.builder()
				.runnerId(member3.getId())
				.status(Running.Status.ENDED)
				.startedAt(now.minusHours(1))
				.endedAt(now)
				.totalDistanceMeter(5000.0)
				.durationSeconds(3600L)
				.avgSpeedMPS(2.5)
				.build());

			HttpHeaders httpHeaders = tokenIssuer.issue(leader.getId(), "USER");

			ParameterizedTypeReference<ApiResponse<CrewResponse.Members>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Members>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType,
					savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().members()).hasSize(3);
			assertThat(response.getBody().getResult().members())
				.extracting("memberId")
				.containsExactlyInAnyOrder(leader.getId(), member2.getId(), member3.getId());
			assertThat(response.getBody().getResult().members())
				.extracting("nickname")
				.containsExactlyInAnyOrder(leader.getNickname().value(), member2.getNickname().value(),
					member3.getNickname().value());
			assertThat(response.getBody().getResult().members())
				.extracting("badgeImageUrl")
				.containsExactlyInAnyOrder("/badge1.png", "/badge1.png", "/badge1.png");
			assertThat(response.getBody().getResult().members())
				.extracting("isRunning")
				.containsExactlyInAnyOrder(false, true, false);
			assertThat(response.getBody().getResult().members())
				.extracting("runningDistance")
				.containsExactlyInAnyOrder(0.0, 10000.0, 5000.0);
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

			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

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
			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

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

			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

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
			Member delegate = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id2"), "name2"));
			Crew crew = Crew.of(new CrewCommand.Create(userId, "Crew"), new Code("abc123"));
			crew.joinMember(2L);
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

			ParameterizedTypeReference<ApiResponse<CrewResponse.Delegate>> responseType = new ParameterizedTypeReference<>() {
			};

			CrewRequest.Delegate request = new CrewRequest.Delegate(delegate.getId());
			ResponseEntity<ApiResponse<CrewResponse.Delegate>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders),
					responseType, savedCrew.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().leaderId()).isEqualTo(delegate.getId());
			assertThat(response.getBody().getResult().leaderNickname()).isEqualTo(delegate.getNickname().value());
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
			Member leader = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id2"), "name2"));
			Member banMember = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id3"), "name3"));
			CrewMemberCount count1 = CrewMemberCount.of(1L);
			count1.increment();
			CrewMemberCount count2 = CrewMemberCount.of(2L);
			count2.increment();
			crewRepository.save(count1);
			crewRepository.save(count2);
			Crew crew = Crew.of(new CrewCommand.Create(1L, "Crew"), new Code("abc123"));
			crew.joinMember(banMember.getId());
			Crew savedCrew = crewRepository.save(crew);

			HttpHeaders httpHeaders = tokenIssuer.issue(userId, "USER");

			ParameterizedTypeReference<ApiResponse<CrewResponse.Ban>> responseType = new ParameterizedTypeReference<>() {
			};

			ResponseEntity<ApiResponse<CrewResponse.Ban>> response =
				testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE, new HttpEntity<>(httpHeaders), responseType,
					savedCrew.getId(), banMember.getId());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getResult().targetId()).isEqualTo(banMember.getId());
			assertThat(response.getBody().getResult().nickname()).isEqualTo(banMember.getNickname().value());
		}
	}
}
