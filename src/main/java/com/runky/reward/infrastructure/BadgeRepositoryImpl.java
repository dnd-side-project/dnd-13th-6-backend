package com.runky.reward.infrastructure;

import com.runky.reward.domain.Badge;
import com.runky.reward.domain.BadgeRepository;
import com.runky.reward.domain.UserBadge;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BadgeRepositoryImpl implements BadgeRepository {

    private final BadgeJpaRepository badgeJpaRepository;
    private final UserBadgeJpaRepository userBadgeJpaRepository;

    @Override
    public UserBadge save(UserBadge userBadge) {
        return userBadgeJpaRepository.save(userBadge);
    }

    @Override
    public Badge save(Badge badge) {
        return badgeJpaRepository.save(badge);
    }

    @Override
    public List<Badge> findBadgesOfUser(Long userId) {
        return badgeJpaRepository.findCharactersByUserId(userId);
    }
}
