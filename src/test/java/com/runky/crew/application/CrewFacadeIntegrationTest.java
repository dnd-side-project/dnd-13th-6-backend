package com.runky.crew.application;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
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

@SpringBootTest
class CrewFacadeIntegrationTest {

	@Autowired
	private CrewFacade crewFacade;
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
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Nested
	@DisplayName("크루 목록 조회 시,")
	class GetDeviceTokenCrews {

		@Test
		@DisplayName("각 크루의 정보를 제공한다")
		void returnMemberBadgeImages() {
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

			List<CrewResult.Card> cards = crewFacade.getCrews(1L);

			assertThat(cards.size()).isEqualTo(2);
			assertThat(cards)
				.extracting("badgeImageUrls")
				.containsExactlyInAnyOrder(
					List.of("/badge1.png", "/badge2.png"),
					List.of("/badge1.png", "/badge2.png", "/badge3.png")
				);
			assertThat(cards)
				.extracting("memberCount")
				.containsExactlyInAnyOrder(2L, 3L);
			assertThat(cards)
				.extracting("isLeader")
				.containsExactlyInAnyOrder(true, false);
			assertThat(cards)
				.extracting("crewName")
				.containsExactlyInAnyOrder("crew1", "crew2");
			assertThat(cards)
				.extracting("crewId")
				.containsExactlyInAnyOrder(crew1.getId(), crew2.getId());
			assertThat(cards)
				.extracting("isRunning")
				.containsExactlyInAnyOrder(false, true);
			assertThat(cards)
				.extracting("goal")
				.containsExactlyInAnyOrder(new BigDecimal("10.50"), new BigDecimal("10.50"));
			assertThat(cards)
				.extracting("runningDistance")
				.containsExactlyInAnyOrder(10000.0, 20000.0);
		}
	}

	@Nested
	@DisplayName("크루 상세 조회 시,")
	class GetDeviceTokenCrewDetail {
		@Test
		@DisplayName("크루에 대한 상세 정보를 반환한다.")
		void returnCrewDetail() {
			Member member = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id1"), "name1"));
			Crew crew = Crew.of(new CrewCommand.Create(member.getId(), "crew 1"), new Code("abc123"));
			crew.joinMember(2L);
			crew.joinMember(3L);
			crew.joinMember(4L);
			crewRepository.save(crew);

			CrewGoalSnapshot snapshot = new CrewGoalSnapshot(crew.getId(), new Goal(new BigDecimal("10.5")), false,
				WeekUnit.from(LocalDate.now().minusWeeks(1)));
			goalRepository.save(snapshot);

			Running running1 = Running.builder()
				.runnerId(member.getId())
				.status(Running.Status.ENDED)
				.startedAt(LocalDateTime.now().minusHours(1))
				.endedAt(LocalDateTime.now())
				.totalDistanceMeter(10000.0)
				.durationSeconds(3600L)
				.avgSpeedMPS(2.5)
				.build();
			runningRepository.save(running1);

			CrewCriteria.Detail criteria = new CrewCriteria.Detail(crew.getId(), member.getId());
			CrewResult.Detail result = crewFacade.getCrew(criteria);

			assertThat(result.crewId()).isEqualTo(crew.getId());
			assertThat(result.goal()).isEqualTo("10.50");
			assertThat(result.runningDistance()).isEqualTo(10000.0);
			assertThat(result.name()).isEqualTo("crew 1");
			assertThat(result.leaderNickname()).isEqualTo("name1");
			assertThat(result.isLeader()).isTrue();
		}
	}

	@Nested
	@DisplayName("크루원 목록 조회 시,")
	class GetDeviceTokenCrewMembers {

		@Test
		@DisplayName("크루원의 이번주 뛴 거리를 포함한 정보를 반환한다.")
		void returnDistance() {
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

			CrewCriteria.Members criteria = new CrewCriteria.Members(savedCrew.getId(), leader.getId());
			List<CrewResult.CrewMember> results = crewFacade.getCrewMembers(criteria);

			assertThat(results.size()).isEqualTo(3);
			assertThat(results)
				.extracting("memberId")
				.containsExactlyInAnyOrder(leader.getId(), member2.getId(), member3.getId());
			assertThat(results)
				.extracting("nickname")
				.containsExactlyInAnyOrder("name1", "name2", "name3");
			assertThat(results)
				.extracting("badgeImageUrl")
				.containsExactlyInAnyOrder("/badge1.png", "/badge1.png", "/badge1.png");
			assertThat(results)
				.extracting("isRunning")
				.containsExactlyInAnyOrder(false, true, false);
			assertThat(results)
				.extracting("runningDistance")
				.containsExactlyInAnyOrder(0.0, 10000.0, 5000.0);
		}
	}
}
