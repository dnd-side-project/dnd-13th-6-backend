package com.runky.reward.domain;

import com.runky.goal.domain.WeekUnit;
import java.util.Optional;

public interface CloverRepository {

    Clover save(Clover clover);

    CrewCloverHistory save(CrewCloverHistory history);

    MemberCloverHistory save(MemberCloverHistory history);

    Optional<Clover> findByUserId(Long userId);

    Optional<Clover> findByUserIdWithLock(Long userId);

    void addClover(Long userId, Long amount);

    void addCloverInCrew(Long crewId, Long amount);

    Optional<CrewCloverHistory> findCrewCloverHistory(Long crewId, WeekUnit weekUnit);

    Optional<MemberCloverHistory> findMemberCloverHistory(Long memberId, WeekUnit weekUnit);
}
