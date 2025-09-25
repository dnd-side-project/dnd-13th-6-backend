package com.runky.goal.batch;

public record MemberGoalAchieveInfo(
        Long memberId,
        boolean isAchieved
) {
}
