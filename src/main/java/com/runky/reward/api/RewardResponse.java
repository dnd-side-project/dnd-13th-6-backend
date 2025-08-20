package com.runky.reward.api;

import java.util.List;

public class RewardResponse {

    public record Characters(
            List<String> characters
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
