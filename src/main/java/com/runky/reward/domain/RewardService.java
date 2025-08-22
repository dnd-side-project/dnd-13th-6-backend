package com.runky.reward.domain;

import com.runky.reward.domain.RewardCommand.GetBadges;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final BadgeRepository badgeRepository;

    @Transactional(readOnly = true)
    public List<Badge> getBadges(GetBadges command) {
        return badgeRepository.findBadgesOfUser(command.userId());
    }
}
