package com.runky.reward.domain;

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

    private RewardCommand() {
    }
}
