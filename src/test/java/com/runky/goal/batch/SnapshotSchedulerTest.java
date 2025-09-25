package com.runky.goal.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.Goal;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import com.runky.reward.domain.Clover;
import com.runky.reward.domain.CloverRepository;
import com.runky.running.domain.RunningRepository;
import com.runky.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBatchTest
@SpringBootTest(classes = {TestBatchConfig.class})
class SnapshotSchedulerTest {

    @Autowired
    @Qualifier("weeklyGoalAchieveJobTest")
    private JobLauncherTestUtils weeklyGoalAchieveJobTest;
    @Autowired
    @Qualifier("weeklyGoalSnapshotJobTest")
    private JobLauncherTestUtils weeklyGoalSnapshotJobTest;
    @Autowired
    @Qualifier("weeklyCrewGoalAchieveJobTest")
    private JobLauncherTestUtils weeklyCrewGoalAchieveJobTest;
    @Autowired
    private CrewRepository crewRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private CloverRepository cloverRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("스냅샷 생성 시, 개인 목표와 크루 목표 스냅샷이 생성된다.")
    void createSnapshots() throws Exception {
        MemberGoal memberGoal1 = MemberGoal.from(1L);
        memberGoal1.updateGoal(new BigDecimal("10.01"));
        goalRepository.save(memberGoal1);
        MemberGoal memberGoal2 = MemberGoal.from(2L);
        memberGoal2.updateGoal(new BigDecimal("10.02"));
        goalRepository.save(memberGoal2);
        MemberGoal memberGoal3 = MemberGoal.from(3L);
        memberGoal3.updateGoal(new BigDecimal("10.03"));
        goalRepository.save(memberGoal3);
        MemberGoal memberGoal4 = MemberGoal.from(4L);
        memberGoal4.updateGoal(new BigDecimal("10.04"));
        goalRepository.save(memberGoal4);

        Crew crew = Crew.of(new CrewCommand.Create(1L, "crew1"), new Code("code12"));
        crew.joinMember(2L);
        crew.joinMember(3L);
        crew.joinMember(4L);
        crew.joinMember(5L);
        crew.leaveMember(5L);
        Crew savedCrew = crewRepository.save(crew);

        LocalDate snapshotDate = LocalDate.of(2025, 8, 25);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters();

        weeklyGoalSnapshotJobTest.launchJob(jobParameters);

        List<MemberGoalSnapshot> snapshots = goalRepository.findLatestSnapshotsOfWeek(Set.of(1L, 2L, 3L, 4L),
                WeekUnit.from(snapshotDate));
        assertThat(snapshots).hasSize(4);
        assertThat(snapshots).extracting("memberId")
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
        assertThat(snapshots).extracting("goal.value")
                .containsExactlyInAnyOrder(memberGoal1.getGoal().value(), memberGoal2.getGoal().value(),
                        memberGoal3.getGoal().value(), memberGoal4.getGoal().value());

        CrewGoalSnapshot crewGoalSnapshot = goalRepository.findCrewGoalSnapshotOfWeek(savedCrew.getId(),
                WeekUnit.from(snapshotDate)).orElseThrow();
        assertThat(crewGoalSnapshot.getGoal().value()).isEqualTo(new BigDecimal("40.10"));
    }

    @Test
    @DisplayName("개인 목표 달성 체크 시, 목표를 달성한 회원만 보상 지급이 호출된다.")
    void checkGoalAchieve() throws Exception {
        LocalDate snapshotDate = LocalDate.of(2025, 8, 25);
        cloverRepository.save(Clover.of(1L));
        cloverRepository.save(Clover.of(2L));
        MemberGoalSnapshot memberGoalSnapshot1 = new MemberGoalSnapshot(1L, new Goal(BigDecimal.TEN), false,
                snapshotDate.minusWeeks(1));
        memberGoalSnapshot1.addDistance(new BigDecimal("10.00"));
        memberGoalSnapshot1.achieve();
        MemberGoalSnapshot memberGoalSnapshot2 = new MemberGoalSnapshot(2L, new Goal(new BigDecimal("15.00")), false,
                snapshotDate.minusWeeks(1));
        memberGoalSnapshot2.addDistance(new BigDecimal("10.00"));
        goalRepository.saveAll(List.of(memberGoalSnapshot1, memberGoalSnapshot2));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("snapshotDate", snapshotDate.toString())
                .toJobParameters();

        weeklyGoalAchieveJobTest.launchJob(jobParameters);

        Clover clover1 = cloverRepository.findByUserId(1L).orElseThrow();
        Clover clover2 = cloverRepository.findByUserId(2L).orElseThrow();
        assertThat(clover1.getCount()).isEqualTo(1L);
        assertThat(clover2.getCount()).isEqualTo(0L);
        MemberGoalSnapshot snapshot1 = goalRepository.findMemberGoalSnapshotOfWeek(1L,
                WeekUnit.from(snapshotDate.minusWeeks(1))).orElseThrow();
        MemberGoalSnapshot snapshot2 = goalRepository.findMemberGoalSnapshotOfWeek(2L,
                WeekUnit.from(snapshotDate.minusWeeks(1))).orElseThrow();
        assertThat(snapshot1.getAchieved()).isTrue();
        assertThat(snapshot2.getAchieved()).isFalse();
    }

    @Test
    @DisplayName("크루 목표 달성 체크 시, 목표를 달성한 회원은 클로버 보상을 받는다.")
    void checkCrewGoalAchieve() throws Exception {
        Crew crew = Crew.of(new CrewCommand.Create(1L, "crew1"), new Code("code12"));
        crew.joinMember(2L);
        crew.joinMember(3L);
        Crew savedCrew = crewRepository.save(crew);
        CrewGoalSnapshot crewGoalSnapshot = new CrewGoalSnapshot(savedCrew.getId(), new Goal(new BigDecimal("30.00")),
                false, WeekUnit.from(LocalDate.of(2025, 8, 18)));
        crewGoalSnapshot.addDistance(new BigDecimal("35.00"));
        crewGoalSnapshot.achieve();
        goalRepository.saveAllCrewGoalSnapshots(List.of(crewGoalSnapshot));

        cloverRepository.save(Clover.of(1L));
        cloverRepository.save(Clover.of(2L));
        cloverRepository.save(Clover.of(3L));

        LocalDate snapshotDate = LocalDate.of(2025, 8, 25);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters();

        weeklyCrewGoalAchieveJobTest.launchJob(jobParameters);

        Clover clover1 = cloverRepository.findByUserId(1L).orElseThrow();
        Clover clover2 = cloverRepository.findByUserId(2L).orElseThrow();
        Clover clover3 = cloverRepository.findByUserId(3L).orElseThrow();
        assertAll(
                () -> assertThat(clover1.getCount()).isEqualTo(3L),
                () -> assertThat(clover2.getCount()).isEqualTo(3L),
                () -> assertThat(clover3.getCount()).isEqualTo(3L)
        );
    }
}