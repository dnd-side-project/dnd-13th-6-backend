package com.runky.reward.domain;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository {

    MemberBadge save(MemberBadge memberBadge);

    Badge save(Badge badge);

    List<Badge> findBadgesOf(Long memberId);

    Optional<Badge> findBadge(Long badgeId);

    boolean hasBadge(Long memberId, Long badgeId);
}
