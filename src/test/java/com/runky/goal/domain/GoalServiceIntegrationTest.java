package com.runky.goal.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class GoalServiceIntegrationTest {

	@Autowired
	private GoalService goalService;
	@Autowired
	private GoalRepository goalRepository;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Nested
	@DisplayName("모든 멤버 목표 스냅샷 저장 시,")
	class SaveAllMemberSnapshots {

		@Test
		@DisplayName("멤버의 현 목표의 스냅샷을 생성한다.")
		void saveSnapshots() {
			MemberGoal memberGoal1 = MemberGoal.from(1L);
			memberGoal1.updateGoal(new BigDecimal("12.05"));
			goalRepository.save(memberGoal1);
			MemberGoal memberGoal2 = MemberGoal.from(2L);
			memberGoal2.updateGoal(new BigDecimal("10.05"));
			goalRepository.save(memberGoal2);

			LocalDate date = LocalDate.of(2025, 8, 24);
			goalService.saveAllMemberSnapshots(new GoalCommand.Snapshot(date));

			Optional<MemberGoalSnapshot> latest1 = goalRepository.findMemberGoalSnapshotOfWeek(1L, WeekUnit.from(date));
			assertThat(latest1).isPresent();
			assertThat(latest1.get().getGoal().value()).isEqualTo(new BigDecimal("12.05"));
			assertThat(latest1.get().getWeekUnit().isoYear()).isEqualTo(2025);
			assertThat(latest1.get().getWeekUnit().isoWeek()).isEqualTo(34);
			Optional<MemberGoalSnapshot> latest2 = goalRepository.findMemberGoalSnapshotOfWeek(2L, WeekUnit.from(date));
			assertThat(latest2).isPresent();
			assertThat(latest2.get().getGoal().value()).isEqualTo(new BigDecimal("10.05"));
			assertThat(latest2.get().getWeekUnit().isoYear()).isEqualTo(2025);
			assertThat(latest2.get().getWeekUnit().isoWeek()).isEqualTo(34);
		}
	}

	@Nested
	@DisplayName("멤버 이번주 목표 조회 시,")
	class GetDeviceTokenMemberGoalSnapshot {

		@Test
		@DisplayName("목표가 생성되지 않은 유저의 경우, 목표 거리가 0인 스냅샷을 반환한다.")
		void returnZeroGoalSnapshot_whenMemberHasNoSnapshot() {
			LocalDate date = LocalDate.of(2025, 8, 26);

			MemberGoalSnapshot snapshot = goalService.getMemberGoalSnapshot(
				new GoalCommand.GetMemberSnapshot(1L, date));

			assertThat(snapshot.getMemberId()).isEqualTo(1L);
			assertThat(snapshot.getGoal().value()).isEqualTo(new BigDecimal("0.00"));
			assertThat(snapshot.getWeekUnit().isoYear()).isEqualTo(2025);
			assertThat(snapshot.getWeekUnit().isoWeek()).isEqualTo(35);
		}
	}

	@Nested
	@DisplayName("사용자 러닝 거리 업데이트 시,")
	class UpdateDistances {
		@Test
		@DisplayName("해당 사용자의 스냅샷 러닝 거리가 업데이트 된다.")
		void updateMemberSnapshotDistance() {
			MemberGoal memberGoal = MemberGoal.from(1L);
			memberGoal.updateGoal(new BigDecimal("15.00"));
			MemberGoalSnapshot memberSnapshot = memberGoal.createSnapshot(LocalDate.of(2025, 10, 1));
			CrewGoalSnapshot crewSnapshot1 = CrewGoalSnapshot.of(List.of(memberSnapshot), 1L,
				LocalDate.of(2025, 10, 1));
			CrewGoalSnapshot crewSnapshot2 = CrewGoalSnapshot.of(List.of(memberSnapshot), 2L,
				LocalDate.of(2025, 10, 1));
			goalRepository.save(memberSnapshot);
			goalRepository.saveAllCrewGoalSnapshots(List.of(crewSnapshot1, crewSnapshot2));
			LocalDate date = LocalDate.of(2025, 10, 2);

			goalService.updateDistances(
				new GoalCommand.UpdateDistance(1L, Set.of(1L, 2L), new BigDecimal("3.50"), date));

			MemberGoalSnapshot snapshot = goalRepository.findMemberGoalSnapshotOfWeek(1L, WeekUnit.from(date))
				.orElseThrow();
			assertThat(snapshot.getRunDistance()).isEqualTo(new BigDecimal("3.50"));

			List<CrewGoalSnapshot> crewSnapshots = goalRepository.findAllCrewGoalSnapshots(Set.of(1L, 2L),
				WeekUnit.from(date));
			assertThat(crewSnapshots)
				.extracting("runDistance")
				.containsExactlyInAnyOrder(new BigDecimal("3.50"), new BigDecimal("3.50"));
		}
	}

	@Nested
	@DisplayName("목표 정보 제거 시,")
	class CleanUp {
		@Test
		@DisplayName("사용자의 MemberGoal과 스냅샷 정보들이 삭제된다.")
		void cleanUp() {
			MemberGoal memberGoal = MemberGoal.from(1L);
			memberGoal.updateGoal(new BigDecimal("15.00"));
			goalRepository.save(memberGoal);
			LocalDate date = LocalDate.of(2025, 10, 1);
			MemberGoalSnapshot snapshot1 = memberGoal.createSnapshot(date);
			MemberGoalSnapshot snapshot2 = memberGoal.createSnapshot(date.minusWeeks(1));
			goalRepository.save(snapshot1);
			goalRepository.save(snapshot2);

			goalService.cleanUp(new GoalCommand.Clean(1L));

			assertThat(goalRepository.findMemberGoalByMemberId(1L)).isEmpty();
			assertThat(goalRepository.findMemberGoalSnapshotOfWeek(1L, WeekUnit.from(date))).isEmpty();
			assertThat(goalRepository.findMemberGoalSnapshotOfWeek(1L, WeekUnit.from(date.minusWeeks(1)))).isEmpty();
		}
	}
}
