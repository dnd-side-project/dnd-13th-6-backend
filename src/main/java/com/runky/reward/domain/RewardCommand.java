package com.runky.reward.domain;

public class RewardCommand {

    public record GetBadges(
            Long userId
    ) {
    }

    private RewardCommand() {
    }
}
