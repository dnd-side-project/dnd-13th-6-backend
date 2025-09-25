package com.runky.goal.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
