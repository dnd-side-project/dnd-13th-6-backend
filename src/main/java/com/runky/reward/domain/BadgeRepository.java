package com.runky.reward.domain;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository {

    UserBadge save(UserBadge userBadge);

    Badge save(Badge badge);

    List<Badge> findBadgesOfUser(Long userId);

    Optional<Badge> findBadge(Long badgeId);
}
