package com.runky.reward.infrastructure;

import com.runky.reward.domain.Clover;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloverJpaRepository extends JpaRepository<Clover, Long> {

    Optional<Clover> findByUserId(Long userId);
}
