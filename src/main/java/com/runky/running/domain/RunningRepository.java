package com.runky.running.domain;

import com.runky.running.domain.RunningInfo.RunningResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RunningRepository {
    boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);
  
    boolean existsByIdAndStatus(Long id, Running.Status status);

    Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

    Running save(Running running);

    List<RunningResult> findTotalDistancesPeriod(LocalDateTime from, LocalDateTime to);
  
	  Optional<Long> findRunnerIdById(Long id);

}
