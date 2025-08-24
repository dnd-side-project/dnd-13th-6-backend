package com.runky.reward.api;

import java.util.List;

public class RewardResponse {

    public record Images(
            List<Image> badges
    ) {
    }

    public record Image(
            String badge
    ) {
    }

    public record Draw(
            String character
    ) {
    }

    public record Clover(
            Long count
    ) {
    }

    private RewardResponse() {
    }
}
