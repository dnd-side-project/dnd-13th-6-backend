package com.runky.reward.application;

import com.runky.reward.domain.Badge;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RewardFacade {

    private final RewardService rewardService;

    public List<RewardResult.Image> getMyCharacters(RewardCriteria.User criteria) {
        List<Badge> badges = rewardService.getBadges(new RewardCommand.GetBadges(criteria.userId()));
        return badges.stream()
                .map(RewardResult.Image::from)
                .toList();
    }
}
