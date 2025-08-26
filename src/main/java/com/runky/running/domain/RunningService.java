package com.runky.running.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.global.error.GlobalException;
import com.runky.running.error.RunningErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningService {

	private final RunningRepository runningRepository;
	private final RunningTrackRepository trackRepository;

	@Transactional
	public RunningInfo.Start start(RunningCommand.Start command) {
		if (runningRepository.existsByRunnerIdAndEndedAtIsNull(command.runnerId())) {
			throw new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING);
		}

		Running running = runningRepository.save(Running.start(command.runnerId(), LocalDateTime.now()));
		return RunningInfo.Start.from(running);
	}

	@Transactional
	public RunningInfo.End end(RunningCommand.End command) {
		Running running = runningRepository.findByIdAndRunnerId(command.runningId(), command.runnerId())
			.orElseThrow(() -> new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING));

		if (!running.isActive()) {
			throw new GlobalException(RunningErrorCode.NOT_ACTIVE_RUNNING);
		}

		LocalDateTime now = LocalDateTime.now();
		running.finish(command.totalDistanceMinutes(), command.durationSeconds(), command.avgSpeedMPS(), now);
		runningRepository.save(running);

		if (trackRepository.existsByRunningId(command.runningId())) {
			throw new GlobalException(RunningErrorCode.TRACK_ALREADY_EXISTS);
		}

		RunningTrack runningTrack = new RunningTrack(
			running,
			command.format(),
			command.points(),
			command.pointCount()
		);
		trackRepository.save(runningTrack);

		return new RunningInfo.End(running.getId(), running.getRunnerId(), running.getStatus().toString(),
			running.getStartedAt(), running.getEndedAt());
	}

	@Transactional(readOnly = true)
	public List<RunningInfo.RunningResult> getTotalDistancesPeriod(LocalDateTime from, LocalDateTime to) {
		return runningRepository.findTotalDistancesPeriod(from, to);
	}

	@Transactional(readOnly = true)
	public boolean isActive(final Long runningId) {
		return runningRepository.existsByIdAndStatus(runningId, Running.Status.RUNNING);
	}

	@Transactional(readOnly = true)
	public Long getRunnerId(final Long runningId) {
		return runningRepository.findRunnerIdById(runningId)
			.orElseThrow(() -> new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING));
	}
}
