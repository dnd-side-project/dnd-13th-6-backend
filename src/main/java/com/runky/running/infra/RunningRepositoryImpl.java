package com.runky.running.infra;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RunningRepositoryImpl implements RunningRepository {
	private final RunningJpaRepository jpaRepository;

	@Override
	public boolean existsByIdAndStatus(final Long id, final Running.Status status) {
		return jpaRepository.existsByIdAndStatus(id, status);
	}

	@Override
	public boolean existsByRunnerIdAndEndedAtIsNull(final Long runnerId) {
		return jpaRepository.existsByRunnerIdAndEndedAtIsNull(runnerId);
	}

	@Override
	public Optional<Running> findByIdAndRunnerId(final Long id, final Long runnerId) {
		return jpaRepository.findByIdAndRunnerId(id, runnerId);
	}

	@Override
	public Optional<Long> findRunnerIdById(final Long id) {
		return jpaRepository.findRunnerIdById(id);
	}

	@Override
	public Running save(final Running running) {
		jpaRepository.save(running);
		return running;
	}
}
