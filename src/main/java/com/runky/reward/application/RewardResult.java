package com.runky.reward.application;

public class RewardResult {

    public record Badge(
            String ImageUrl,
            String name
    ) {
    }

    public record Clover(
            Long userId,
            Long count
    ) {
    }

    public record Gotcha(
            Long badgeId,
            String name,
            String imageUrl
    ) {
    }
}
