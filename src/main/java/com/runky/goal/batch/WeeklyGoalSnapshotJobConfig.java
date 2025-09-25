package com.runky.goal.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyGoalSnapshotJobConfig {

    @Bean
    public Job weeklyGoalSnapshotJob(Step memberGoalSnapshotStep,
                                     Step crewGoalSnapshotStep,
                                     JobRepository jobRepository) {
        return new JobBuilder("weeklyGoalSnapshotJob", jobRepository)
                .start(memberGoalSnapshotStep)
                .next(crewGoalSnapshotStep)
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
    public Step crewGoalSnapshotStep(JobRepository jobRepository,
                                     PlatformTransactionManager tm,
                                     CrewGoalReader reader,
                                     CrewGoalProcessor processor,
                                     CrewGoalWriter writer) {
        return new StepBuilder("crewGoalSnapshotStep", jobRepository)
                .<CrewGoalSum, CrewGoalSnapshot>chunk(300, tm)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public CrewGoalReader crewGoalReader(EntityManagerFactory emf) {
        return new CrewGoalReader(emf);
    }

    @Bean
    @StepScope
    public CrewGoalProcessor crewGoalProcessor(@Value("#{jobParameters['snapshotDate']}") LocalDate date) {
        return new CrewGoalProcessor(date);
    }

    @Bean
    @StepScope
    public CrewGoalWriter crewGoalWriter(GoalRepository goalRepository) {
        return new CrewGoalWriter(goalRepository);
    }
}
