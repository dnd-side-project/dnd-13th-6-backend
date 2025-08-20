package com.runky.running.domain;

public interface RunningTrackRepository {
	boolean existsByRunningId(Long runningId);

	void save(RunningTrack runningTrack);

}
