package com.runky.running.domain;

import java.util.Optional;

public interface RunningTrackRepository {
	boolean existsByRunningId(Long runningId);

	void save(RunningTrack runningTrack);

	Optional<RunningTrack> findByRunning(Running running);
}
