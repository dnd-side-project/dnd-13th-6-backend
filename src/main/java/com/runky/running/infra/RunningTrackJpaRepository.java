package com.runky.running.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runky.running.domain.RunningTrack;

public interface RunningTrackJpaRepository extends JpaRepository<RunningTrack, Long> {
	boolean existsByRunningId(Long runningId);
}
