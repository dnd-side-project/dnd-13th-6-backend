package com.runky.goal.application.step;

import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.batch.MemberGoalAchieveInfo;
import com.runky.goal.domain.batch.WeeklyMemberGoalProcessor;
import com.runky.goal.domain.batch.WeeklyMemberGoalReader;
import com.runky.goal.domain.batch.WeeklyMemberGoalWriter;
import com.runky.reward.domain.CloverRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
public class WeeklyGoalAchieveStepConfig {

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
                                                         @Value("#{jobParameters['snapshotDate']}") LocalDate date) {
        return new WeeklyMemberGoalReader(emf, date);
    }

    @Bean
    @StepScope
    public WeeklyMemberGoalProcessor weeklyMemberGoalProcessor() {
        return new WeeklyMemberGoalProcessor();
    }

    @Bean
    @StepScope
    public WeeklyMemberGoalWriter weeklyMemberGoalWriter(CloverRepository cloverRepository,
                                                         @Value("#{jobParameters['snapshotDate']}") LocalDate date) {
        return new WeeklyMemberGoalWriter(cloverRepository, date);
    }
}
