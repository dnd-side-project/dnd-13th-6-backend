package com.runky.reward.domain;

import java.util.Optional;

public interface CloverRepository {

    Clover save(Clover clover);

    Optional<Clover> findByUserId(Long userId);

    Optional<Clover> findByUserIdWithLock(Long userId);

    void addClover(Long userId, Long amount);

    void addCloverInCrew(Long crewId, Long amount);
}
