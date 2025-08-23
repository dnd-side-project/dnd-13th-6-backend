package com.runky.reward.infrastructure;

import com.runky.reward.domain.Badge;
import com.runky.reward.domain.BadgeRepository;
import com.runky.reward.domain.MemberBadge;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BadgeRepositoryImpl implements BadgeRepository {

    private final BadgeJpaRepository badgeJpaRepository;
    private final MemberBadgeJpaRepository memberBadgeJpaRepository;

    @Override
    public MemberBadge save(MemberBadge memberBadge) {
        return memberBadgeJpaRepository.save(memberBadge);
    }

    @Override
    public Badge save(Badge badge) {
        return badgeJpaRepository.save(badge);
    }

    @Override
    public List<Badge> findBadgesOf(Long memberId) {
        return badgeJpaRepository.findBadgesOf(memberId);
    }

    @Override
    public Optional<Badge> findBadge(Long badgeId) {
        return badgeJpaRepository.findById(badgeId);
    }

    @Override
    public boolean hasBadge(Long memberId, Long badgeId) {
        return memberBadgeJpaRepository.existsByMemberIdAndBadgeId(memberId, badgeId);
    }
}
