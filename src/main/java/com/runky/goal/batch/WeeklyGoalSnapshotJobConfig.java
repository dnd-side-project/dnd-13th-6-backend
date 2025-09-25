package com.runky.goal.batch;

import com.runky.crew.domain.CrewActiveMemberInfo;
import com.runky.crew.domain.CrewService;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalCommand;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.GoalService;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyGoalSnapshotJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GoalService goalService;
    private final CrewService crewService;

    @Bean
    public Job weeklyGoalSnapshotJob(Step memberGoalSnapshotStep) {
        return new JobBuilder("weeklyGoalSnapshotJob", jobRepository)
                .start(memberGoalSnapshotStep)
                .next(crewGoalSnapshotStep())
                .build();
    }

    @Bean
    public Step memberGoalSnapshotStep(JobRepository jobRepository,
                                       PlatformTransactionManager tm,
                                       MemberGoalReader reader,
                                       MemberGoalProcessor processor,
                                       MemberGoalWriter writer) {
        return new StepBuilder("memberGoalSnapshotStep", jobRepository)
                .<MemberGoal, MemberGoalSnapshot>chunk(500, tm)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public MemberGoalReader memberGoalReader(EntityManagerFactory emf) {
        return new MemberGoalReader(emf);
    }

    @Bean
    @StepScope
    public MemberGoalProcessor memberGoalProcessor(@Value("#{jobParameters['snapshotDate']}") LocalDate date) {
        return new MemberGoalProcessor(date);
    }

    @Bean
    @StepScope
    public MemberGoalWriter memberGoalWriter(GoalRepository goalRepository) {
        return new MemberGoalWriter(goalRepository);
    }

    @Bean
    public Step crewGoalSnapshotStep() {
        return new StepBuilder("crewMemberFindStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution()
                            .getJobParameters();
                    LocalDate snapshotDate = jobParameters.getLocalDate("snapshotDate");

                    // 현재 크루 내 활동중인 멤버 조회
                    List<CrewActiveMemberInfo> infos = crewService.getActiveMembersInfo();

                    List<CrewGoalSnapshot> crewGoalSnapshots = new ArrayList<>();
                    for (CrewActiveMemberInfo info : infos) {
                        GoalCommand.CrewSnapshot command =
                                new GoalCommand.CrewSnapshot(info.crewId(), info.memberIds(), snapshotDate);

                        // 크루원들의 개인 목표들을 통해 이번주 크루 목표 생성
                        CrewGoalSnapshot crewSnapshot = goalService.createCrewSnapshot(command);
                        crewGoalSnapshots.add(crewSnapshot);
                    }

                    goalService.saveAllCrewSnapshots(crewGoalSnapshots);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
