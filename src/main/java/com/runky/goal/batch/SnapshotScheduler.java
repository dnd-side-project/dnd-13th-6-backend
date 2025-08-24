package com.runky.goal.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class SnapshotScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklySnapshotJob;
    private final Job weeklyGoalAchieveJob;

    public SnapshotScheduler(JobLauncher jobLauncher,
                             @Qualifier("weeklyGoalSnapshotJob") Job weeklySnapshotJob,
                             @Qualifier("weeklyGoalAchieveJob") Job weeklyGoalAchieveJob) {
        this.jobLauncher = jobLauncher;
        this.weeklySnapshotJob = weeklySnapshotJob;
        this.weeklyGoalAchieveJob = weeklyGoalAchieveJob;
    }

    @Scheduled(cron = "0 0 2 * * MON")
    public void runWeeklySnapshotJob() throws Exception {
        LocalDate snapshotDate = LocalDate.now();

        log.info("스냅샷 배치 실행 - {}", LocalDateTime.now());
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters();

        jobLauncher.run(weeklySnapshotJob, jobParameters);
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklyGoalAchieveJob() throws Exception {
        log.info("주간 목표 달성 확인 배치 실행 - {}", LocalDateTime.now());
        LocalDate snapshotDate = LocalDate.now();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("snapshotDate", snapshotDate.toString())
                .toJobParameters();

        jobLauncher.run(weeklyGoalAchieveJob, jobParameters);
    }
}
