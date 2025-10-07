package com.runky.reward.domain;

import com.runky.global.error.GlobalException;
import com.runky.reward.domain.Gotcha.Capsule;
import com.runky.reward.error.RewardErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardService {
    private static final Gotcha DEFAULT_GOTCHA = Gotcha.DEFAULT;

    private final BadgeRepository badgeRepository;
    private final CloverRepository cloverRepository;

    @Transactional(readOnly = true)
    public List<Badge> getBadges(RewardCommand.GetBadges command) {
        return badgeRepository.findBadgesOf(command.userId());
    }

    @Transactional(readOnly = true)
    public Badge getBadge(RewardCommand.Find command) {
        return badgeRepository.findBadge(command.badgeId())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_BADGE));
    }

    @Transactional(readOnly = true)
    public Badge getMemberBadge(RewardCommand.FindMemberBadge command) {
        if (!badgeRepository.hasBadge(command.userId(), command.badgeId())) {
            throw new GlobalException(RewardErrorCode.NOT_OWNED_BADGE);
        }
        return badgeRepository.findBadge(command.badgeId())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_BADGE));
    }

    public long calculateMemberGoalClover(RewardCommand.Count command) {
        return CloverRewardStrategy.MEMBER_GOAL.apply(command.count());
    }

    public long calculateCrewGoalClover(RewardCommand.Count command) {
        return CloverRewardStrategy.CREW_GOAL.apply(command.count());
    }

    @Transactional(readOnly = true)
    public Clover getClover(RewardCommand.GetClover command) {
        return cloverRepository.findByUserId(command.userId())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_CLOVER));
    }

    @Transactional
    public void init(RewardCommand.Init command) {
        List<Badge> defaultBadges = badgeRepository.findByType(Badge.BadgeType.DEFAULT);
        List<MemberBadge> memberBadges = defaultBadges.stream()
                .map(badge -> badge.issue(command.memberId()))
                .toList();
        badgeRepository.save(memberBadges);
        Clover clover = Clover.of(command.memberId());
        cloverRepository.save(clover);
    }

    @Transactional
    public Badge gotcha(RewardCommand.Gotcha command) {
        Capsule capsule = DEFAULT_GOTCHA.random();

        Badge badge = badgeRepository.findBadgeByName(capsule.getName())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_BADGE));
        MemberBadge memberBadge = badge.issue(command.memberId());

        Clover clover = cloverRepository.findByUserIdWithLock(command.memberId())
                .orElseThrow(() -> new GlobalException(RewardErrorCode.NOT_FOUND_CLOVER));
        clover.useForGotcha();

        try {
            badgeRepository.save(memberBadge);
        } catch (DataIntegrityViolationException ignored) {
        }
        return badge;
    }
}
