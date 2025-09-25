package com.runky.goal.domain.batch;

public record MemberGoalAchieveInfo(
        Long memberId,
        boolean isAchieved
) {
}
