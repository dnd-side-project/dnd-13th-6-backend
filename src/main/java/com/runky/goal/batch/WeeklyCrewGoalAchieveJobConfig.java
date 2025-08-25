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

                    // 모든 사용자의 저번 주 뛴 거리 총합 조회
                    List<RunningInfo.RunningResult> results = runningService.getTotalDistancesPeriod(
                            snapshotDate.minusDays(7).atStartOfDay(),
                            snapshotDate.minusDays(1).atTime(LocalTime.MAX));

                    // 유저 ID - 뛴 거리 Map으로 변환
                    Map<Long, Double> userDistanceMap = results.stream()
                            .collect(Collectors.toMap(
                                    RunningInfo.RunningResult::runnerId,
                                    RunningInfo.RunningResult::distance,
                                    Double::sum)
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
                    // 모든 크루의 활동중 멤버 조회
                    List<CrewActiveMemberInfo> infos = crewService.getActiveMembersInfo();
                    for (CrewActiveMemberInfo info : infos) {

                        // 크루 내 멤버들의 주간 거리 합산
                        Double sum = 0.0;
                        for (Long memberId : info.memberIds()) {
                            sum += map.getOrDefault(memberId, 0.0);
                        }

                        // 저번 주 크루 목표 조회
                        Optional<CrewGoalSnapshot> crewSnapshot = goalService.findCrewGoalSnapshot(
                                info.crewId(), snapshotDate.minusWeeks(1));
                        if (crewSnapshot.isPresent()) {
                            CrewGoalSnapshot snapshot = crewSnapshot.get();
                            // 크루 목표 달성 시, 달성 멤버 ID 목록 추가 및 크루 목표 달성 처리
                            if (sum >= snapshot.getGoal().value().doubleValue()) {
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

                    // 달성한 멤버들에게 클로버 보상 지급
                    rewardService.achieveCrewGoal(achieverIds);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }
}
