package com.runky.running.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.global.error.GlobalException;
import com.runky.running.api.WsDestinations;
import com.runky.running.error.RunningErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningService {

	private final RunningRepository runningRepository;
	private final RunningTrackRepository trackRepository;

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
		running.finish(command.totalDistanceMinutes(), command.durationSeconds(), command.avgSpeedMPS(), now);
		runningRepository.save(running);

		RunningTrack runningTrack = new RunningTrack(
			running,
			command.points(),
			command.format(),
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
	public List<RunningInfo.RunnerStatusAndSub> getRunnerStatusesAndSub(final Collection<Long> runnerIds) {
		if (runnerIds == null || runnerIds.isEmpty()) {
			return List.of();
		}

		// 입력 순서 보존
		final List<Long> orderedIds = runnerIds.stream()
			.filter(Objects::nonNull)
			.toList();

		final List<Long> distinctIds = orderedIds.stream()
			.distinct()
			.toList();

		final Set<Long> activeRunnerIds = runningRepository
			.findRunnerIdsByStatusAndEndedAtIsNull(distinctIds, Running.Status.RUNNING);

		if (activeRunnerIds.isEmpty()) {
			return orderedIds.stream()
				.map(id -> new RunningInfo.RunnerStatusAndSub(id, false, null))
				.toList();
		}

		final Map<Long, Long> runningIdByRunnerId = new HashMap<>(activeRunnerIds.size());
		for (Long runnerId : activeRunnerIds) {
			runningRepository.findByRunnerIdAndStatusAndEndedAtIsNull(runnerId, Running.Status.RUNNING)
				.map(Running::getId)
				.ifPresent(runningId -> runningIdByRunnerId.put(runnerId, runningId));
		}

		return orderedIds.stream()
			.map(runnerId -> {
				final Long runningId = runningIdByRunnerId.get(runnerId);
				final boolean active = (runningId != null);
				final String sub = active ? WsDestinations.subscribe(runningId) : null;
				return new RunningInfo.RunnerStatusAndSub(runnerId, active, sub);
			})
			.toList();
	}

	@Transactional(readOnly = true)
	public List<RunningInfo.RunnerStatus> getRunnerStatuses(final List<Long> runnerIds) {
		if (runnerIds == null || runnerIds.isEmpty())
			return List.of();

		final List<Long> ordered = runnerIds.stream().filter(Objects::nonNull).toList();

		final Set<Long> active = runningRepository
			.findRunnerIdsByStatusAndEndedAtIsNull(ordered, Running.Status.RUNNING);

		return ordered.stream()
			.map(id -> new RunningInfo.RunnerStatus(id, active.contains(id)))
			.toList();
	}

	@Transactional(readOnly = true)
	public RunningInfo.TodaySummary getTodaySummary(final Long runnerId, final LocalDateTime now) {

		List<Running> runs = runningRepository.findFinishedOnDate(runnerId, now);
		if (runs.isEmpty()) {
			throw new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING);
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

	@Transactional(readOnly = true)
	public RunningInfo.MyWeek getMyWeeklyTotalDistance(RunningCommand.MyWeeklyTotalDistance command) {
		LocalDate today = LocalDate.now();
		LocalDate weekStart = toWeekStart(today);
		LocalDate weekEnd = toWeekEnd(weekStart);

		var from = weekStart.atStartOfDay(); // inclusive
		var toExclusive = weekEnd.plusDays(1).atStartOfDay(); // exclusive

		List<Running> runnings = runningRepository.findBetweenFromAndToByRunnerId(
			command.runnerId(), from, toExclusive
		);

		double totalMeters = runnings.stream()
			.filter(r -> r.getStatus() == Running.Status.ENDED)
			.map(Running::getTotalDistanceMeter)
			.filter(Objects::nonNull)
			.mapToDouble(Double::doubleValue)
			.sum();

		return new RunningInfo.MyWeek(command.runnerId(), totalMeters, weekStart, weekEnd);
	}

	private LocalDate toWeekStart(LocalDate date) {
		return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}

	private LocalDate toWeekEnd(LocalDate weekStart) {
		return weekStart.plusDays(6);
	}
}
