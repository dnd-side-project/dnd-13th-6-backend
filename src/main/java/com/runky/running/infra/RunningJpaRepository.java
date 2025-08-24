package com.runky.running.infra;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RunningJpaRepository extends JpaRepository<Running, Long> {
    boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

    Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

    @Query("SELECT new com.runky.running.domain.RunningInfo$RunningResult(r.runnerId, SUM(r.totalDistanceMeter)) " +
            "FROM Running r " +
            "WHERE r.status = com.runky.running.domain.Running.Status.FINISHED " +
            "AND r.startedAt >= :from " +
            "AND r.endedAt <= :to " +
            "GROUP BY r.runnerId")
    List<RunningInfo.RunningResult> findRunnerDistanceResultsByPeriod(
            LocalDateTime from,
            LocalDateTime to
    );
}
