package com.runky.reward.infrastructure;

import com.runky.reward.domain.Clover;
import com.runky.reward.domain.CloverRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CloverRepositoryImpl implements CloverRepository {

    private final CloverJpaRepository cloverJpaRepository;

    @Override
    public Clover save(Clover clover) {
        return cloverJpaRepository.save(clover);
    }

    @Override
    public Optional<Clover> findByUserId(Long userId) {
        return cloverJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Clover> findByUserIdWithLock(Long userId) {
        return cloverJpaRepository.findByUserIdWithLock(userId);
    }
}
