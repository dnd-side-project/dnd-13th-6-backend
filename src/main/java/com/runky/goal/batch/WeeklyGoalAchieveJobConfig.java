package com.runky.goal.batch;

import com.runky.goal.domain.GoalService;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.reward.domain.RewardService;
import com.runky.running.domain.Running;
import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningInfo.TotalDistance;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyGoalAchieveJobConfig {

    private final JobRepository jobRepository;
    private final GoalService goalService;
    private final RewardService rewardService;

    @Bean
    public Job weeklyGoalAchieveJob(Step weeklyGoalAchieveStep) {
        return new JobBuilder("weeklyGoalAchieveJob", jobRepository)
                .start(weeklyGoalAchieveStep)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<RunningInfo.TotalDistance> weeklyDistanceReader(EntityManagerFactory emf,
                                                                               @Value("#{jobParameters['snapshotDate']}") String dateString) {
        LocalDate snapshotDate = LocalDate.parse(dateString);
        return new JpaPagingItemReaderBuilder<RunningInfo.TotalDistance>()
                .name("weeklyDistanceReader")
                .entityManagerFactory(emf)
                .queryString(
                        "SELECT new com.runky.running.domain.RunningInfo$TotalDistance(r.runnerId, SUM(r.totalDistanceMeter)) "
                                +
                                "FROM Running r " +
                                "WHERE r.status = :status " +
                                "AND r.startedAt >= :from " +
                                "AND r.endedAt <= :to " +
                                "GROUP BY r.runnerId"
                )
                .parameterValues(Map.of(
                        "status", Running.Status.FINISHED,
                        "from", snapshotDate.minusDays(7).atStartOfDay(),
                        "to", snapshotDate.minusDays(1).atTime(LocalTime.MAX)
                ))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<TotalDistance, Long> goalAchieveProcessor(
            @Value("#{jobParameters['snapshotDate']}") String dateString) {
        LocalDate snapshotDate = LocalDate.parse(dateString);
        return totalDistance -> {
            Optional<MemberGoalSnapshot> snapshot =
                    goalService.findLastWeekMemberGoalSnapshot(totalDistance.runnerId(), snapshotDate);
            if (snapshot.isEmpty()) {
                return null;
            }
            if (snapshot.get().getGoal().value().doubleValue() <= totalDistance.totalDistance()) {
                snapshot.get().achieve();
                return totalDistance.runnerId();
            }
            return null;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Long> goalAchieveWriter() {
        return achievedMembers -> {
            for (Long memberId : achievedMembers) {
                rewardService.achieveMemberGoal(memberId);
            }
        };
    }

    @Bean
    public Step weeklyGoalAchieveStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                      JpaPagingItemReader<RunningInfo.TotalDistance> reader,
                                      ItemProcessor<RunningInfo.TotalDistance, Long> processor,
                                      ItemWriter<Long> writer) {
        return new StepBuilder("weeklyGoalAchieveStep", jobRepository)
                .<RunningInfo.TotalDistance, Long>chunk(100, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
