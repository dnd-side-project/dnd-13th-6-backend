package com.runky.goal.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
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
import org.springframework.boot.test.context.SpringBootTest;

@SpringBatchTest
@SpringBootTest
class SnapshotSchedulerTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private CrewRepository crewRepository;
    @Autowired
    private GoalRepository goalRepository;
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

        LocalDate snapshotDate = LocalDate.of(2025, 7, 24);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters();

        jobLauncherTestUtils.launchJob(jobParameters);

        List<MemberGoalSnapshot> snapshots = goalRepository.findLatestSnapshots(Set.of(1L, 2L, 3L, 4L),
                WeekUnit.from(snapshotDate));
        assertThat(snapshots).hasSize(4);
        assertThat(snapshots).extracting("memberId")
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
        assertThat(snapshots).extracting("goal.value")
                .containsExactlyInAnyOrder(memberGoal1.getGoal().value(), memberGoal2.getGoal().value(),
                        memberGoal3.getGoal().value(), memberGoal4.getGoal().value());

        CrewGoalSnapshot crewGoalSnapshot = goalRepository.findLatestCrewGoalSnapshot(savedCrew.getId()).orElseThrow();
        assertThat(crewGoalSnapshot.getGoal().value()).isEqualTo(new BigDecimal("40.10"));
    }
}