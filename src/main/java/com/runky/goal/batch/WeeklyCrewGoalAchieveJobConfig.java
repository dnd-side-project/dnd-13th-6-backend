// src/main/java/com/runky/goal/batch/WeeklyCrewGoalAchieveJobConfig.java
package com.runky.goal.batch;

import com.runky.crew.domain.CrewActiveMemberInfo;
import com.runky.crew.domain.CrewService;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalService;
import com.runky.reward.domain.RewardService;
import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyCrewGoalAchieveJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final CrewService crewService;
    private final RewardService rewardService;
    private final RunningService runningService;
    private final GoalService goalService;

    @Bean
    public Job weeklyCrewGoalAchieveJob() {
        return new JobBuilder("weeklyCrewGoalAchieveJob", jobRepository)
                .start(collectWeeklyDistancesStep())
                .next(checkCrewGoalsStep())
                .next(rewardAchieversStep())
                .build();
    }

    @Bean
    public Step collectWeeklyDistancesStep() {
        return new StepBuilder("collectWeeklyDistancesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution()
                            .getJobParameters();
                    LocalDate snapshotDate = jobParameters.getLocalDate("snapshotDate");

                    List<RunningInfo.RunningResult> results = runningService.getTotalDistancesPeriod(
                            snapshotDate.minusDays(7).atStartOfDay(),
                            snapshotDate.minusDays(1).atTime(LocalTime.MAX));

                    Map<Long, Double> userDistanceMap = results.stream()
                            .collect(Collectors.toMap(
                                    RunningInfo.RunningResult::runnerId,
                                    RunningInfo.RunningResult::distance)
                            );

                    ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext();
                    executionContext.put("map", userDistanceMap);

                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    @Bean
    public Step checkCrewGoalsStep() {
        return new StepBuilder("checkCrewGoalsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution()
                            .getJobParameters();
                    LocalDate snapshotDate = jobParameters.getLocalDate("snapshotDate");

                    ExecutionContext ctx = chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext();
                    Map<Long, Double> map = (Map<Long, Double>) ctx.get("map");

                    List<Long> achieverIds = new ArrayList<>();
                    List<CrewActiveMemberInfo> infos = crewService.getActiveMembersInfo();
                    for (CrewActiveMemberInfo info : infos) {
                        Double sum = 0.0;
                        for (Long memberId : info.memberIds()) {
                            sum += map.getOrDefault(memberId, 0.0);
                        }
                        Optional<CrewGoalSnapshot> crewSnapshot = goalService.findCrewGoalSnapshot(
                                info.crewId(), snapshotDate.minusDays(7));
                        if (crewSnapshot.isPresent()) {
                            CrewGoalSnapshot snapshot = crewSnapshot.get();
                            if (sum <= snapshot.getGoal().value().doubleValue()) {
                                achieverIds.addAll(info.memberIds());
                                snapshot.achieve();
                            }
                        }
                    }

                    ctx.put("achieverIds", achieverIds);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    @Bean
    public Step rewardAchieversStep() {
        return new StepBuilder("rewardAchieversStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext ctx = chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext();
                    List<Long> achieverIds = (List<Long>) ctx.get("achieverIds");
                    rewardService.achieveCrewGoal(achieverIds);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }
}