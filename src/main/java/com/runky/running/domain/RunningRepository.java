package com.runky.running.domain;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.runky.running.domain.RunningInfo.RunningResult;

public interface RunningRepository {
	boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

	boolean existsByIdAndStatus(Long id, Running.Status status);

	Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

	Running save(Running running);

	List<RunningResult> findTotalDistancesPeriod(LocalDateTime from, LocalDateTime to);

	Optional<Long> findRunnerIdById(Long id);

	boolean existsByRunnerIdAndStatusAndEndedAtIsNull(Long memberId, Running.Status status);

	Set<Long> findRunnerIdsByStatusAndEndedAtIsNull(final Collection<Long> runnerIds, final Running.Status status);

	List<Running> findFinishedOnDate(Long runnerId, LocalDateTime now);

    List<Running> findBetweenFromAndToByRunnerId(Long runnerId, LocalDateTime from, LocalDateTime to);
}
