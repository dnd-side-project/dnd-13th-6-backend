package com.runky.running.infra;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningInfo;

public interface RunningJpaRepository extends JpaRepository<Running, Long> {
	boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

	Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

	@Query("SELECT new com.runky.running.domain.RunningInfo$RunningResult(r.runnerId, SUM(r.totalDistanceMeter)) " +
		"FROM Running r " +
		"WHERE r.status = com.runky.running.domain.Running.Status.ENDED " +
		"AND r.startedAt >= :from " +
		"AND r.endedAt <= :to " +
		"GROUP BY r.runnerId")
	List<RunningInfo.RunningResult> findRunnerDistanceResultsByPeriod(
		LocalDateTime from,
		LocalDateTime to
	);

	boolean existsByIdAndStatus(Long id, Running.Status status);

	@Query("select r.runnerId from Running r where r.id = :id")
	Optional<Long> findRunnerIdById(Long id);

	boolean existsByRunnerIdAndStatusAndEndedAtIsNull(Long runnerId, Running.Status status);

	@Query("""
		select distinct r.runnerId
		  from Running r
		 where r.status = :status
		   and r.endedAt is null
		   and r.runnerId in :runnerIds
		""")
	Set<Long> findRunnerIdsByStatusAndEndedAtIsNull(Collection<Long> runnerIds, Running.Status status);

	@Query("""
			select r
			  from Running r
			 where r.runnerId = :runnerId
			   and r.status = com.runky.running.domain.Running$Status.ENDED
			   and r.endedAt >= :from and r.endedAt < :to
			 order by r.endedAt desc
		""")
	List<Running> findFinishedByEndedAtBetween(Long runnerId, LocalDateTime from, LocalDateTime to);

	@Query("""
		select r
		from Running r
		where r.runnerId = :runnerId
		and r.endedAt >= :from and r.endedAt < :to
		""")
	List<Running> findBetweenFromAndToByRunnerId(Long runnerId, LocalDateTime from, LocalDateTime to);

	Optional<Running> findByRunnerIdAndStatusAndEndedAtIsNull(Long memberId, Running.Status status);
}
