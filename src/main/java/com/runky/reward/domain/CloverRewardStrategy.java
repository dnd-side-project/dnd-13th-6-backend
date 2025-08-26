package com.runky.reward.domain;

import lombok.Getter;

@Getter
public enum CloverRewardStrategy {
    MEMBER_GOAL(1) {
        @Override
        public long apply(long count) {
            return count * this.getValue();
        }
    },
    CREW_GOAL(3) {
        @Override
        public long apply(long count) {
            return count * this.getValue();
        }
    };

    private final long value;

    public abstract long apply(long count);

    CloverRewardStrategy(int value) {
        this.value = value;
    }
}
