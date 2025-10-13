package com.runky.crew.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class CrewServiceIntegrationTest {

	@Autowired
	private CrewService crewService;
	@Autowired
	private CrewRepository crewRepository;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Test
	@DisplayName("사용자가 속한 크루를 조회한다.")
	void getCrewsOfUser() {
		crewRepository.save(Crew.of(new CrewCommand.Create(1L, "Crew 1"), new Code("abc123")));
		crewRepository.save(Crew.of(new CrewCommand.Create(1L, "Crew 2"), new Code("abc123")));
		crewRepository.save(Crew.of(new CrewCommand.Create(1L, "Crew 3"), new Code("abc123")));

		List<Crew> crews = crewService.getCrewsOfUser(1L);

		assertThat(crews).hasSize(3);
	}

	@Nested
	@DisplayName("크루 목록 조회 시,")
	class GetDeviceTokenCrews {
		@Test
		@DisplayName("떠난 크루는 포함되지 않는다.")
		void notIncludeLeftCrew() {
			Crew crew1 = Crew.of(new CrewCommand.Create(1L, "Crew 1"), new Code("ABC123"));
			crew1.joinMember(2L);
			crew1.joinMember(3L);
			crewRepository.save(crew1);
			Crew crew2 = Crew.of(new CrewCommand.Create(1L, "Crew 2"), new Code("DEF456"));
			crew2.joinMember(2L);
			crew2.joinMember(3L);
			crew2.leaveMember(3L);
			crewRepository.save(crew2);

			List<Crew> crews = crewService.getCrewsOfUser(3L);

			assertThat(crews).hasSize(1);
			assertThat(crews).extracting("name").containsExactly("Crew 1");
		}
	}

	@Nested
	@DisplayName("크루 생성 시,")
	class Create {
		@Test
		@DisplayName("속한 크루 개수가 증가한다.")
		void incrementCrewCount() {
			crewRepository.save(CrewMemberCount.of(1L));
			CrewCommand.Create command = new CrewCommand.Create(1L, "Test Crew");

			crewService.create(command);

			CrewMemberCount crewMemberCount = crewRepository.findCountByMemberId(1L).orElseThrow();
			assertThat(crewMemberCount.getCrewCount()).isEqualTo(1L);
		}

		@Test
		@DisplayName("생성한 사용자는 크루의 리더가 된다.")
		void createLeader() {
			crewRepository.save(CrewMemberCount.of(1L));
			CrewCommand.Create command = new CrewCommand.Create(1L, "Test Crew");

			Crew crew = crewService.create(command);

			CrewMember crewMember = crewRepository.findByCrewAndMember(crew.getId(), 1L).orElseThrow();
			assertThat(crew.getLeaderId()).isEqualTo(crewMember.getMemberId());
			assertThat(crewMember.isLeader()).isTrue();
		}
	}

	@Nested
	@DisplayName("크루 가입 시,")
	class Join {

		@Test
		@DisplayName("재가입하는 경우, 기존 CrewMember의 상태가 MEMBER로 변경된다.")
		void rejoinCrewMember() {
			Crew crew = Crew.of(new CrewCommand.Create(1L, "Crew"), new Code("ABC123"));
			crew.joinMember(2L);
			crew.joinMember(3L);
			CrewMember before = crew.leaveMember(3L);
			crewRepository.save(CrewMemberCount.of(3L));
			Crew saveCrew = crewRepository.save(crew);

			Crew joinedCrew = crewService.join(new CrewCommand.Join(3L, saveCrew.getCode().value()));

			assertThat(before.getRole()).isEqualTo(CrewMember.Role.LEFT);
			assertThat(joinedCrew.getMember(3L).getRole()).isEqualTo(CrewMember.Role.MEMBER);
		}
	}

	@Nested
	@DisplayName("모든 관련 크루원 조회 시,")
	class RelatedCrewMembers {

		@Test
		@DisplayName("내가 활동중인 크루의 모든 활동 멤버들을 조회한다.")
		void getRelatedCrewMembers() {
			Crew crew1 = Crew.of(new CrewCommand.Create(1L, "Crew 1"), new Code("ABC123"));
			crew1.joinMember(2L);
			crew1.joinMember(3L);
			crew1.leaveMember(3L);
			Crew crew2 = Crew.of(new CrewCommand.Create(1L, "Crew 2"), new Code("DEF456"));
			crew2.joinMember(10L);
			crew2.joinMember(11L);
			crew2.joinMember(13L);
			crew2.banMember(13L);
			Crew saveCrew1 = crewRepository.save(crew1);
			Crew saveCrew2 = crewRepository.save(crew2);

			List<CrewMember> relatedMembers = crewService.findAllRelatedCrewMembers(new CrewCommand.Related(1L));

			assertThat(relatedMembers).hasSize(3);
			assertThat(relatedMembers).extracting("memberId").containsExactlyInAnyOrder(2L, 10L, 11L);
		}
	}

	@Nested
	@DisplayName("사용자 모든 크루 탈퇴 시,")
	class CleanUp {
		@Test
		@DisplayName("리더로 속한 크루일 경우, 무작위로 리더를 위임하고, 탈퇴한다.")
		void cleanUpCrewsOfUser_asLeader() {
			CrewMemberCount crewMemberCount = CrewMemberCount.of(1L);
			Crew crew = Crew.of(new CrewCommand.Create(1L, "Crew 1"), new Code("ABC123"));
			crew.joinMember(2L);
			crew.joinMember(3L);
			crewMemberCount.increment();
			crewRepository.save(crewMemberCount);
			Crew saveCrew = crewRepository.save(crew);

			crewService.cleanUp(new CrewCommand.Clean(1L));

			Crew find = crewRepository.findById(saveCrew.getId()).orElseThrow();
			assertThat(find.doesNotContainMember(1L)).isTrue();
			assertThat(find.getLeaderId()).isIn(2L, 3L);
		}

		@Test
		@DisplayName("멤버로 속한 크루일 경우, 탈퇴한다.")
		void cleanUpCrewsOfUser_asMember() {
			Crew crew = Crew.of(new CrewCommand.Create(10L, "Crew 1"), new Code("ABC123"));
			crew.joinMember(1L);
			CrewMemberCount crewMemberCount = CrewMemberCount.of(1L);
			crewMemberCount.increment();
			crewRepository.save(crewMemberCount);
			Crew saveCrew = crewRepository.save(crew);

			crewService.cleanUp(new CrewCommand.Clean(1L));

			Crew find = crewRepository.findById(saveCrew.getId()).orElseThrow();
			assertThat(find.doesNotContainMember(1L)).isTrue();
		}

		@Test
		@DisplayName("크루 멤버 정보가 삭제된다.")
		void deleteCrewMembers() {
			CrewMemberCount crewMemberCount = CrewMemberCount.of(1L);
			Crew crew = Crew.of(new CrewCommand.Create(10L, "Crew 1"), new Code("ABC123"));
			crew.joinMember(1L);
			crewMemberCount.increment();
			Crew leaderCrew = Crew.of(new CrewCommand.Create(1L, "Crew 2"), new Code("DEF456"));
			leaderCrew.joinMember(2L);
			crewMemberCount.increment();
			crewRepository.save(crew);
			crewRepository.save(leaderCrew);
			crewRepository.save(crewMemberCount);

			crewService.cleanUp(new CrewCommand.Clean(1L));

			List<CrewMember> relatedCrewMembers = crewRepository.findRelatedCrewMembers(1L);
			assertThat(relatedCrewMembers).isEmpty();
		}

		@Test
		@DisplayName("크루 개수 정보가 삭제된다.")
		void deleteCrewMemberCount() {
			CrewMemberCount crewMemberCount = CrewMemberCount.of(1L);
			Crew crew = Crew.of(new CrewCommand.Create(10L, "Crew 1"), new Code("ABC123"));
			crew.joinMember(1L);
			crewMemberCount.increment();
			crewRepository.save(crew);
			crewRepository.save(crewMemberCount);

			crewService.cleanUp(new CrewCommand.Clean(1L));

			assertThat(crewRepository.findCountByMemberId(1L)).isEmpty();
		}

		@Test
		@DisplayName("본인 밖에 없는 크루라면, 크루는 삭제된다.")
		void deleteCrew_whenAlone() {
			Crew crew = Crew.of(new CrewCommand.Create(1L, "name"), new Code("ABC123"));
			Crew saveCrew = crewRepository.save(crew);

			crewService.cleanUp(new CrewCommand.Clean(1L));

			Optional<Crew> find = crewRepository.findById(saveCrew.getId());
			assertThat(find).isEmpty();
		}
	}

	@Nested
	@DisplayName("동시성 테스트")
	class Concurrency {
		@Test
		@DisplayName("동시에 크루 생성을 시도할 경우, 낙관적 락에 의한 실패 케이스가 존재한다.")
		void createOnlyOneCrew_withConcurrency() throws InterruptedException {
			crewRepository.save(CrewMemberCount.of(1L));
			int threadCount = 10;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch latch = new CountDownLatch(threadCount);
			CountDownLatch start = new CountDownLatch(1);

			for (int i = 0; i < threadCount; i++) {
				CrewCommand.Create command = new CrewCommand.Create(1L, "Test Crew " + i);
				executor.submit(() -> {
					try {
						start.await();
						crewService.create(command);
					} catch (Exception e) {
						System.out.println("실패: " + e.getMessage());
					} finally {
						latch.countDown();
					}
				});
			}
			start.countDown();
			latch.await();

			CrewMemberCount crewMemberCount = crewRepository.findCountByMemberId(1L).orElseThrow();
			List<CrewMember> crewMembers = crewRepository.findCrewMemberOfUser(1L);
			assertThat(crewMembers).hasSizeLessThan(6);
			assertThat(crewMemberCount.getCrewCount()).isLessThan(6);
		}
	}
}
