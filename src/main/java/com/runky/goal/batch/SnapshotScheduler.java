package com.runky.goal.batch;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SnapshotScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklySnapshotJob;

    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklySnapshotJob() throws Exception {
        LocalDate snapshotDate = LocalDate.now();

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters();

        jobLauncher.run(weeklySnapshotJob, jobParameters);
    }
}
