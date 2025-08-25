package com.runky.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.runky.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    class GetMemberGoalSnapshot {

        @Test
        @DisplayName("목표가 생성되지 않은 유저의 경우, 목표 거리가 0인 스냅샷을 반환한다.")
        void returnZeroGoalSnapshot_whenMemberHasNoSnapshot() {
            LocalDate date = LocalDate.of(2025, 8, 26);

            MemberGoalSnapshot snapshot = goalService.getMemberGoalSnapshot(new GoalCommand.GetMemberSnapshot(1L, date));

            assertThat(snapshot.getMemberId()).isEqualTo(1L);
            assertThat(snapshot.getGoal().value()).isEqualTo(new BigDecimal("0.00"));
            assertThat(snapshot.getWeekUnit().isoYear()).isEqualTo(2025);
            assertThat(snapshot.getWeekUnit().isoWeek()).isEqualTo(35);
        }
    }
}