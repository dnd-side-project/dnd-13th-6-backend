package com.runky.reward.application;

import com.runky.reward.domain.Badge;

public class RewardResult {

    public record Image(
            String ImageUrl
    ) {
        public static Image from(Badge image) {
            return new Image(image.getImageUrl());
        }
    }

    public record Clover(
            Long userId,
            Long count
    ) {
    }
}
