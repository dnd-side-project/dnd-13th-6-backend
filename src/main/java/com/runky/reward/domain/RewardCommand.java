package com.runky.reward.domain;

import java.util.List;

public class RewardCommand {

    public record Find(
            Long badgeId
    ) {
    }

    public record GetBadges(
            Long userId
    ) {
    }

    public record FindMemberBadge(
            Long userId,
            Long badgeId
    ) {
    }

    public record GetClover(
            Long userId
    ) {
    }

    public record Count(
            Long count
    ) {
    }

    public record Init(
            Long memberId
    ) {
    }

    public record Gotcha(
            Long memberId
    ) {
    }

    public record Images(
            List<Long> badgeId
    ) {
    }

    private RewardCommand() {
    }
}
