package com.runky.goal.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.reward.domain.CloverRepository;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyCrewGoalAchieveJobConfig {

    @Bean
    public Job weeklyCrewGoalAchieveJob(JobRepository jobRepository,
                                        Step weeklyCrewGoalAchieveStep) {
        return new JobBuilder("weeklyCrewGoalAchieveJob", jobRepository)
                .start(weeklyCrewGoalAchieveStep)
                .build();
    }

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
    public CrewGoalSnapshotReader crewGoalSnapshotReader(EntityManagerFactory emf) {
        return new CrewGoalSnapshotReader(emf);
    }

    @Bean
    @StepScope
    public CrewGoalSnapshotProcessor crewGoalSnapshotProcessor() {
        return new CrewGoalSnapshotProcessor();
    }

    @Bean
    @StepScope
    public CrewGoalSnapshotWriter crewGoalSnapshotWriter(CloverRepository cloverRepository) {
        return new CrewGoalSnapshotWriter(cloverRepository);
    }
}
