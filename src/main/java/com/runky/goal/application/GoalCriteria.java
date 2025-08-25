package com.runky.goal.application;

public class GoalCriteria {

    public record MemberGoal(Long memberId) {
    }

    public record CrewGoal(Long crewId) {
    }
}
