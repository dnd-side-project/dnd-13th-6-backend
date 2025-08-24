package com.runky.reward.domain;

import java.util.List;

public interface BadgeRepository {

    UserBadge save(UserBadge userBadge);

    Badge save(Badge badge);

    List<Badge> findBadgesOfUser(Long userId);
}
