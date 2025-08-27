package com.runky.running.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.running.infra.RunningJpaRepository;
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

	@DisplayName("여러 멤버의 런닝 상태를 일괄 조회하면 (runnerId, isRunning)으로 반환한다 - 중복 runnerId는 제거")
	@Test
	void getRunnerStatuses_bulk_ok() {
		// given
		var now = LocalDateTime.now();

		// 10L: 활성 RUNNING (endedAt = null)
		var active = Running.start(10L, now);
		runningJpaRepository.save(active);

		// 20L: FINISHED (endedAt != null)
		var finished = Running.start(20L, now.minusMinutes(30));
		runningJpaRepository.save(finished);
		finished.finish(5000d, 1800L, 2.7, now.minusMinutes(10));
		runningJpaRepository.save(finished);

		// 30L: 어떤 러닝도 없음 → 비활성

		// when
		var result = runningService.getRunnerStatuses(List.of(10L, 20L, 20L, 30L));

		// then
		// distinct()를 적용하므로 10,20,30 총 3개
		assertThat(result).hasSize(3);

		// (runnerId → isRunning) 맵으로 검증
		Map<Long, Boolean> statusMap = result.stream()
			.collect(Collectors.toMap(
				RunningInfo.RunnerStatus::runnerId,
				RunningInfo.RunnerStatus::isRunning
			));

		assertThat(statusMap.get(10L)).isTrue();   // RUNNING & endedAt IS NULL
		assertThat(statusMap.get(20L)).isFalse();  // FINISHED
		assertThat(statusMap.get(30L)).isFalse();  // 기록 없음
	}
}
