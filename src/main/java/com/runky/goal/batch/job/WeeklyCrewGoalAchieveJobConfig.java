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
public class WeeklyCrewGoalAchieveJobConfig {

    @Bean
    public Job weeklyCrewGoalAchieveJob(JobRepository jobRepository,
                                        Step weeklyCrewGoalAchieveStep) {
        return new JobBuilder("weeklyCrewGoalAchieveJob", jobRepository)
                .start(weeklyCrewGoalAchieveStep)
                .build();
    }
}
