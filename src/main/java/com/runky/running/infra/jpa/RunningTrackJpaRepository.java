package com.runky.running.infra.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningTrack;

public interface RunningTrackJpaRepository extends JpaRepository<RunningTrack, Long> {
	boolean existsByRunningId(Long runningId);

	Optional<RunningTrack> findByRunning(Running running);
}
