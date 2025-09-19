package com.runky.running.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

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
}
