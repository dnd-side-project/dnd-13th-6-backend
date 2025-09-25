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
public class WeeklyGoalAchieveJobConfig {


    @Bean
    public Job weeklyGoalAchieveJob(JobRepository jobRepository,
                                    Step weeklyGoalAchieveStep) {
        return new JobBuilder("weeklyGoalAchieveJob", jobRepository)
                .start(weeklyGoalAchieveStep)
                .build();
    }
}
