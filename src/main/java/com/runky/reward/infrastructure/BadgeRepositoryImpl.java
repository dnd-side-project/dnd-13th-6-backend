package com.runky.reward.infrastructure;

import com.runky.reward.domain.Badge;
import com.runky.reward.domain.Badge.BadgeType;
import com.runky.reward.domain.BadgeRepository;
import com.runky.reward.domain.MemberBadge;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class BadgeRepositoryImpl implements BadgeRepository {

    private final BadgeJpaRepository badgeJpaRepository;
    private final MemberBadgeJpaRepository memberBadgeJpaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MemberBadge save(MemberBadge memberBadge) {
        return memberBadgeJpaRepository.save(memberBadge);
    }

    @Override
    public Badge save(Badge badge) {
        return badgeJpaRepository.save(badge);
    }

    @Override
    public List<MemberBadge> save(List<MemberBadge> memberBadges) {
        return memberBadgeJpaRepository.saveAll(memberBadges);
    }

    @Override
    public List<Badge> findBadgesOf(Long memberId) {
        return badgeJpaRepository.findBadgesOf(memberId);
    }

    @Override
    public List<Badge> findByType(BadgeType badgeType) {
        return badgeJpaRepository.findByType(badgeType);
    }

    @Override
    public Optional<Badge> findBadge(Long badgeId) {
        return badgeJpaRepository.findById(badgeId);
    }

    @Override
    public Optional<Badge> findBadgeByName(String name) {
        return badgeJpaRepository.findByName(name);
    }

    @Override
    public boolean hasBadge(Long memberId, Long badgeId) {
        return memberBadgeJpaRepository.existsByMemberIdAndBadgeId(memberId, badgeId);
    }
}
