package com.runky.running.domain;

import java.util.Optional;

public interface RunningRepository {
	boolean existsByIdAndStatus(Long id, Running.Status status);

	boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

	Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

	Optional<Long> findRunnerIdById(Long id);

	Running save(Running running);
}
