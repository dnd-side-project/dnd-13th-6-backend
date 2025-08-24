package com.runky.reward.domain;

public class RewardCommand {

    public record GetBadges(
            Long userId
    ) {
    }

    public record GetClover(
            Long userId
    ) {
    }

    private RewardCommand() {
    }
}
