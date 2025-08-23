package com.runky.reward.domain;

import com.runky.global.error.GlobalException;
import com.runky.reward.error.RewardErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final BadgeRepository badgeRepository;
    private final CloverRepository cloverRepository;

    @Transactional(readOnly = true)
    public List<Badge> getBadges(RewardCommand.GetBadges command) {
        return badgeRepository.findBadgesOfUser(command.userId());
    }

    @Transactional(readOnly = true)
    public Badge getBadge(RewardCommand.Find command) {
        return badgeRepository.findBadge(command.badgeId())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_BADGE));
    }

    @Transactional(readOnly = true)
    public Clover getClover(RewardCommand.GetClover command) {
        return cloverRepository.findByUserId(command.userId())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_CLOVER));
    }
}
