package com.runky.reward.infrastructure;

import com.runky.reward.domain.Clover;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CloverJpaRepository extends JpaRepository<Clover, Long> {

    Optional<Clover> findByUserId(Long userId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Clover c WHERE c.userId = :userId")
    Optional<Clover> findByUserIdWithLock(Long userId);

    @Modifying
    @Query("UPDATE Clover c SET c.count = c.count + :amount WHERE c.userId = :userId")
    void addClover(Long userId, Long amount);
}
