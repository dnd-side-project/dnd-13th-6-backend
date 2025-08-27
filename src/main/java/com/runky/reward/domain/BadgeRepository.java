package com.runky.reward.domain;

import com.runky.reward.domain.Badge.BadgeType;
import java.util.List;
import java.util.Optional;

public interface BadgeRepository {

    MemberBadge save(MemberBadge memberBadge);

    Badge save(Badge badge);

    List<MemberBadge> save(List<MemberBadge> memberBadges);

    List<Badge> findBadgesOf(Long memberId);

    List<Badge> findByType(BadgeType badgeType);

    Optional<Badge> findBadge(Long badgeId);

    Optional<Badge> findBadgeByName(String name);

    boolean hasBadge(Long memberId, Long badgeId);
}
