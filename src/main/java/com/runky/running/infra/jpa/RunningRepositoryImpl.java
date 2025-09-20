package com.runky.running.infra.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RunningRepositoryImpl implements RunningRepository {

	private final RunningJpaRepository jpaRepository;

	@Override
	public boolean existsByRunnerIdAndEndedAtIsNull(final Long runnerId) {
		return jpaRepository.existsByRunnerIdAndEndedAtIsNull(runnerId);
	}

	@Override
	public Optional<Running> findByIdAndRunnerId(final Long id, final Long runnerId) {
		return jpaRepository.findByIdAndRunnerId(id, runnerId);
	}

	@Override
	public Running save(final Running running) {
		jpaRepository.save(running);
		return running;
	}

	@Override
	public List<RunningInfo.RunningResult> findTotalDistancesPeriod(final LocalDateTime from, final LocalDateTime to) {
		return jpaRepository.findRunnerDistanceResultsByPeriod(from, to);
	}

	@Override
	public boolean existsByIdAndStatus(final Long id, final Running.Status status) {
		return jpaRepository.existsByIdAndStatus(id, status);
	}

	@Override
	public Optional<Long> findRunnerIdById(final Long id) {
		return jpaRepository.findRunnerIdById(id);
	}

	@Override
	public boolean existsByRunnerIdAndStatusAndEndedAtIsNull(final Long memberId, final Running.Status status) {
		return jpaRepository.existsByRunnerIdAndStatusAndEndedAtIsNull(memberId, status);
	}

	@Override
	public Optional<Running> findByRunnerIdAndStatusAndEndedAtIsNull(final Long memberId, final Running.Status status) {
		return jpaRepository.findByRunnerIdAndStatusAndEndedAtIsNull(memberId, status);
	}

	@Override
	public Set<Long> findRunnerIdsByStatusAndEndedAtIsNull(final Collection<Long> runnerIds,
		final Running.Status status) {
		return jpaRepository.findRunnerIdsByStatusAndEndedAtIsNull(runnerIds, status);
	}

	@Override
	public List<Running> findFinishedOnDate(final Long runnerId, final LocalDateTime now) {
		LocalDate day = now.toLocalDate();
		LocalDateTime from = day.atStartOfDay();
		LocalDateTime to = day.plusDays(1).atStartOfDay();
		return jpaRepository.findFinishedByEndedAtBetween(runnerId, from, to);
	}

	@Override
	public List<Running> findBetweenFromAndToByRunnerId(final Long runnerId, final LocalDateTime from,
		final LocalDateTime to) {
		return jpaRepository.findBetweenFromAndToByRunnerId(runnerId, from, to);
	}

	@Override
	public int deleteByIdAndRunnerIdAndStatus(final Long runningId, final Long runnerId, final Running.Status status) {
		return jpaRepository.deleteByIdAndRunnerIdAndStatus(runningId, runnerId, status);
	}

}
