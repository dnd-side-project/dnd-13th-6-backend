package com.runky.goal.batch;

import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.reward.domain.CloverRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyGoalAchieveJobConfig {


    @Bean
    public Job weeklyGoalAchieveJob(JobRepository jobRepository,
                                    Step weeklyGoalAchieveStep) {
        return new JobBuilder("weeklyGoalAchieveJob", jobRepository)
                .start(weeklyGoalAchieveStep)
                .build();
    }

    @Bean
    public Step weeklyGoalAchieveStep(JobRepository jobRepository,
                                      PlatformTransactionManager txManager,
                                      ItemReader<MemberGoalSnapshot> reader,
                                      ItemProcessor<MemberGoalSnapshot, MemberGoalAchieveInfo> processor,
                                      ItemWriter<MemberGoalAchieveInfo> writer) {
        return new StepBuilder("weeklyGoalAchieveStep", jobRepository)
                .<MemberGoalSnapshot, MemberGoalAchieveInfo>chunk(500, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public WeeklyMemberGoalReader weeklyMemberGoalReader(EntityManagerFactory emf,
                                                         @Value("#{jobParameters['snapshotDate']}") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return new WeeklyMemberGoalReader(emf, date);
    }

    @Bean
    @StepScope
    public WeeklyMemberGoalProcessor weeklyMemberGoalProcessor() {
        return new WeeklyMemberGoalProcessor();
    }

    @Bean
    @StepScope
    public WeeklyMemberGoalWriter weeklyMemberGoalWriter(CloverRepository cloverRepository) {
        return new WeeklyMemberGoalWriter(cloverRepository);
    }
}
