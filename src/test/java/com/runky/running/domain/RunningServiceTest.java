package com.runky.running.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.running.infra.jpa.RunningJpaRepository;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class RunningServiceTest {
	@Autowired
	RunningService runningService;
	@Autowired
	RunningJpaRepository runningJpaRepository;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@DisplayName("멤버의 런닝 상태를 멤버Id로 조회할 수 있다.")
	@Test
	void existsByRunnerIdAndStatus_works() {
		var now = LocalDateTime.now();
		var running = Running.start(1L, now);
		runningJpaRepository.save(running);

		assertThat(runningJpaRepository.existsByRunnerIdAndStatusAndEndedAtIsNull(1L, Running.Status.RUNNING)).isTrue();
		assertThat(
			runningJpaRepository.existsByRunnerIdAndStatusAndEndedAtIsNull(1L, Running.Status.ENDED)).isFalse();
	}

    @DisplayName("해당 주의 러닝 기록을 조회한다.")
    @Test
    void getWeeklyHistories() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 6, 10, 0);
        for (int i = 0; i < 7; i++) {
            Running running = Running.start(1L, now.plusDays(i));
            running.finish(5.0, 60 * 30, 6.0, now.plusMinutes(30));
            runningJpaRepository.save(running);
        }
        LocalDateTime end = now.toLocalDate().plusDays(7).atStartOfDay();
        Running excluded = Running.start(1L, end.minusMinutes(30));
        excluded.finish(5.0, 60 * 30, 6.0, end);
        runningJpaRepository.save(excluded);

        List<RunningInfo.History> histories = runningService.getWeeklyHistories(new RunningCommand.Weekly(1L, LocalDate.of(2025, 10, 6)));

        assertThat(histories).hasSize(7);
    }
}
