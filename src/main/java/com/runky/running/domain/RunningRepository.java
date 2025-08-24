package com.runky.running.domain;

import com.runky.running.domain.RunningInfo.RunningResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RunningRepository {
    boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

    Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

    Running save(Running running);

    List<RunningResult> findTotalDistancesPeriod(LocalDateTime from, LocalDateTime to);
}
