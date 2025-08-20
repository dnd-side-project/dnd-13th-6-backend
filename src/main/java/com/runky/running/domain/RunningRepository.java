package com.runky.running.domain;

import java.util.Optional;

public interface RunningRepository {
	boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

	Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

	Running save(Running running);
}
