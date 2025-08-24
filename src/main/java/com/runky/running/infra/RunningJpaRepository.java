package com.runky.running.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.runky.running.domain.Running;

public interface RunningJpaRepository extends JpaRepository<Running, Long> {
	boolean existsByRunnerIdAndEndedAtIsNull(Long runnerId);

	Optional<Running> findByIdAndRunnerId(Long id, Long runnerId);

	boolean existsByIdAndStatus(Long id, Running.Status status);

	@Query("select r.runnerId from Running r where r.id = :id")
	Optional<Long> findRunnerIdById(Long id);

}
