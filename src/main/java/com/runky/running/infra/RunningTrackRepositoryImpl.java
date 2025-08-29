package com.runky.running.infra;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningTrack;
import com.runky.running.domain.RunningTrackRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RunningTrackRepositoryImpl implements RunningTrackRepository {
	private final RunningTrackJpaRepository jpaRepository;

	@Override
	public boolean existsByRunningId(final Long runningId) {
		return jpaRepository.existsByRunningId(runningId);
	}

	@Override
	public void save(final RunningTrack runningTrack) {
		jpaRepository.save(runningTrack);
	}

	@Override
	public Optional<RunningTrack> findByRunning(final Running running) {
		return jpaRepository.findByRunning(running);
	}
}
