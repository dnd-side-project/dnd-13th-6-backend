package com.runky.running.infra;

import com.runky.running.domain.Running;
import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public List<RunningInfo.RunningResult> findTotalDistancesPeriod(LocalDateTime from, LocalDateTime to) {
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
}
