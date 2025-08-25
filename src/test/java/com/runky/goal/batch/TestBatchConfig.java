package com.runky.goal.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestBatchConfig {

    @Bean
    public JobLauncherTestUtils weeklyGoalAchieveJobTest(
            @Qualifier("weeklyGoalAchieveJob") Job job) {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJob(job);
        return utils;
    }

    @Bean
    public JobLauncherTestUtils weeklyGoalSnapshotJobTest(
            @Qualifier("weeklyGoalSnapshotJob") Job job) {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJob(job);
        return utils;
    }

    @Bean
    public JobLauncherTestUtils weeklyCrewGoalAchieveJobTest(
            @Qualifier("weeklyCrewGoalAchieveJob") Job job) {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJob(job);
        return utils;
    }
}
