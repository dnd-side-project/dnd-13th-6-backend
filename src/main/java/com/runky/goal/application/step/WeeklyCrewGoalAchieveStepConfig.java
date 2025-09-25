package com.runky.goal.application.step;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.batch.CrewGoalAchieveInfo;
import com.runky.goal.domain.batch.WeeklyCrewGoalSnapshotProcessor;
import com.runky.goal.domain.batch.WeeklyCrewGoalSnapshotReader;
import com.runky.goal.domain.batch.WeeklyCrewGoalSnapshotWriter;
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
public class WeeklyCrewGoalAchieveStepConfig {

    @Bean
    public Step weeklyCrewGoalAchieveStep(JobRepository jobRepository,
                                          PlatformTransactionManager tm,
                                          ItemReader<CrewGoalSnapshot> reader,
                                          ItemProcessor<CrewGoalSnapshot, CrewGoalAchieveInfo> processor,
                                          ItemWriter<CrewGoalAchieveInfo> writer) {
        return new StepBuilder("weeklyCrewGoalAchieveStep", jobRepository)
                .<CrewGoalSnapshot, CrewGoalAchieveInfo>chunk(500, tm)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public WeeklyCrewGoalSnapshotReader crewGoalSnapshotReader(EntityManagerFactory emf,
                                                               @Value("#{jobParameters['snapshotDate']}") LocalDate date) {
        return new WeeklyCrewGoalSnapshotReader(emf, date);
    }

    @Bean
    @StepScope
    public WeeklyCrewGoalSnapshotProcessor crewGoalSnapshotProcessor() {
        return new WeeklyCrewGoalSnapshotProcessor();
    }

    @Bean
    @StepScope
    public WeeklyCrewGoalSnapshotWriter crewGoalSnapshotWriter(CloverRepository cloverRepository,
                                                               @Value("#{jobParameters['snapshotDate']}") LocalDate date) {
        return new WeeklyCrewGoalSnapshotWriter(cloverRepository, date);
    }
}
