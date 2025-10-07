package com.runky.running.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.global.error.GlobalException;
import com.runky.running.error.RunningErrorCode;
import com.runky.running.interfaces.websocket.WsDestinations;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningService {

	private final RunningRepository runningRepository;
	private final RunningTrackRepository trackRepository;

	@Transactional(readOnly = true)
	public RunningInfo.RunResult getRunResult(RunningCommand.RunResult command) {
		Running running = runningRepository.findByIdAndRunnerId(command.runningId(), command.runnerId())
			.orElseThrow(() -> new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING));

		RunningTrack runningTrack = trackRepository.findByRunning(running)
			.orElseThrow(() -> new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING_TRACK));

		return RunningInfo.RunResult.from(running, runningTrack);

	}

	@Transactional
	public RunningInfo.Start start(RunningCommand.Start command) {
		boolean runnerStatus = getRunnerStatus(command.runnerId());
		if (runnerStatus == true) {
			throw new GlobalException(RunningErrorCode.ALREADY_ACTIVE_RUNNING);
		}

		Running running = runningRepository.save(Running.start(command.runnerId(), LocalDateTime.now()));
		return RunningInfo.Start.from(running);
	}

	@Transactional
	public RunningInfo.End end(RunningCommand.End command) {
		Running running = runningRepository.findByIdAndRunnerId(command.runningId(), command.runnerId())
			.orElseThrow(() -> new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING));

		if (running.getStatus() == Running.Status.ENDED) {
			throw new GlobalException(RunningErrorCode.ALREADY_ENDED_RUNNING);
		}

		if (!running.isActive()) {
			throw new GlobalException(RunningErrorCode.NOT_ACTIVE_RUNNING);
		}

		if (trackRepository.existsByRunningId(command.runningId())) {
			throw new GlobalException(RunningErrorCode.TRACK_ALREADY_EXISTS);
		}

		LocalDateTime now = LocalDateTime.now();
		running.finish(command.totalDistanceMeter(), command.durationSeconds(), command.avgSpeedMPS(), now);
		runningRepository.save(running);

		RunningTrack runningTrack = new RunningTrack(
			running,
			command.points(),
			command.format(),
			command.pointCount()
		);
		trackRepository.save(runningTrack);

		return new RunningInfo.End(running.getId(), running.getRunnerId(), running.getTotalDistanceMeter(),
                running.getStatus().toString(), running.getStartedAt(), running.getEndedAt());
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

	@Transactional(readOnly = true)
	public boolean getRunnerStatus(final Long runnerId) {
		return runningRepository.existsByRunnerIdAndStatusAndEndedAtIsNull(runnerId, Running.Status.RUNNING);
	}

	@Transactional(readOnly = true)
	public RunningInfo.RunnerStatusAndSub getRunnerStatusAndSub(Long runnerId) {
		return runningRepository.findByRunnerIdAndStatusAndEndedAtIsNull(runnerId, Running.Status.RUNNING)
			.map(r -> new RunningInfo.RunnerStatusAndSub(runnerId, true, WsDestinations.subscribe(r.getId())))
			.orElseGet(() -> new RunningInfo.RunnerStatusAndSub(runnerId, false, null));
	}

	
	@Transactional(readOnly = true)
	public RunningInfo.TodaySummary getTodaySummary(final Long runnerId, final LocalDateTime now) {

		List<Running> runs = runningRepository.findFinishedOnDate(runnerId, now);
		if (runs.isEmpty()) {
			return new RunningInfo.TodaySummary(0.0, 0L, 0.0);
		}

		double totalDistance = runs.stream()
			.map(Running::getTotalDistanceMeter)
			.filter(Objects::nonNull)
			.mapToDouble(Double::doubleValue)
			.sum();

		long totalSeconds = runs.stream()
			.map(Running::getDurationSeconds)
			.filter(Objects::nonNull)
			.mapToLong(Long::longValue)
			.sum();

		Double avgSpeedMps = (totalSeconds == 0) ? null : totalDistance / totalSeconds;

		return new RunningInfo.TodaySummary(totalDistance, totalSeconds, avgSpeedMps);
	}

	@Transactional(readOnly = true)
	public RunningInfo.TotalDistance getTotalDistancesOf(RunningCommand.WeekDistance command) {
		List<Running> runnings = runningRepository.findBetweenFromAndToByRunnerId
			(command.runnerId(), command.from(), command.to());

		Double totalDistance = runnings.stream()
			.filter(running -> running.getStatus() == Running.Status.ENDED)
			.map(Running::getTotalDistanceMeter)
			.mapToDouble(Double::doubleValue)
			.sum();

		return new RunningInfo.TotalDistance(command.runnerId(), totalDistance);
	}

    public List<RunningInfo.History> getWeeklyHistories(RunningCommand.Weekly command) {
        LocalDateTime start = command.start().atStartOfDay();
        LocalDateTime end = command.start().plusDays(7).atStartOfDay();

        List<Running> histories = runningRepository.findBetweenFromAndToByRunnerId(command.runnerId(), start, end);

        return histories.stream()
                .filter(Running::isEnded)
                .map(RunningInfo.History::from)
                .toList();
    }

    public List<RunningInfo.History> getMonthlyHistories(RunningCommand.Monthly command) {
        LocalDateTime start = LocalDate.of(command.year(), command.month(), 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<Running> histories = runningRepository.findBetweenFromAndToByRunnerId(command.runnerId(), start, end);

        return histories.stream()
                .filter(Running::isEnded)
                .map(RunningInfo.History::from)
                .toList();
    }

	public int removeActiveRunning(RunningCommand.RemoveActiveRunning command) {
		return runningRepository.deleteByIdAndRunnerIdAndStatus(
			command.runningId(),
			command.runnerId(),
			Running.Status.RUNNING
		);
	}
}
