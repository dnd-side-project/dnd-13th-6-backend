package com.runky.reward.infrastructure;

import com.runky.goal.domain.WeekUnit;
import com.runky.reward.domain.Clover;
import com.runky.reward.domain.CloverRepository;
import com.runky.reward.domain.CrewCloverHistory;
import com.runky.reward.domain.MemberCloverHistory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CloverRepositoryImpl implements CloverRepository {
    private final CloverJpaRepository cloverJpaRepository;
    private final CrewCloverHistoryJpaRepository crewCloverHistoryJpaRepository;
    private final MemberCloverHistoryJpaRepository memberCloverHistoryJpaRepository;

    @Override
    public Clover save(Clover clover) {
        return cloverJpaRepository.save(clover);
    }

    @Override
    public CrewCloverHistory save(CrewCloverHistory history) {
        return crewCloverHistoryJpaRepository.save(history);
    }

    @Override
    public MemberCloverHistory save(MemberCloverHistory history) {
        return memberCloverHistoryJpaRepository.save(history);
    }

    @Override
    public Optional<Clover> findByUserId(Long userId) {
        return cloverJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Clover> findByUserIdWithLock(Long userId) {
        return cloverJpaRepository.findByUserIdWithLock(userId);
    }

    @Override
    public void addClover(Long userId, Long amount) {
        cloverJpaRepository.addClover(userId, amount);
    }

    @Override
    public void addCloverInCrew(Long crewId, Long amount) {
        cloverJpaRepository.addCloverInCrew(crewId, amount);
    }

    @Override
    public Optional<CrewCloverHistory> findCrewCloverHistory(Long crewId, WeekUnit weekUnit) {
        return crewCloverHistoryJpaRepository.findByCrewIdAndWeekUnit(crewId, weekUnit);
    }

    @Override
    public Optional<MemberCloverHistory> findMemberCloverHistory(Long memberId, WeekUnit weekUnit) {
        return memberCloverHistoryJpaRepository.findByMemberIdAndWeekUnit(memberId, weekUnit);
    }
}
