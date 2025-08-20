package com.runky.running.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runky.running.domain.Running;

public interface RunningJpaRepository extends JpaRepository<Running, Long> {
	boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

	Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

}
