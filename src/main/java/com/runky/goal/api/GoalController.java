package com.runky.goal.api;

import com.runky.global.response.ApiResponse;
import com.runky.goal.application.CrewGoalSnapshotResult;
import com.runky.goal.application.GoalCriteria;
import com.runky.goal.application.GoalFacade;
import com.runky.goal.application.MemberGoalResult;
import com.runky.goal.application.MemberGoalSnapshotResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController implements GoalApiSpec {

    private final GoalFacade goalFacade;

    @Override
    @PatchMapping("/me")
    public ApiResponse<GoalResponse.Goal> updateGoal(@RequestBody GoalRequest.Goal request,
                                                     @RequestHeader("X-USER-ID") Long userId) {
        MemberGoalResult result = goalFacade.updateMemberGoal(new GoalCriteria.Update(userId, request.goal()));
        return ApiResponse.success(new GoalResponse.Goal(result.goal()));
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<GoalResponse.Goal> getGoal(@RequestHeader("X-USER-ID") Long userId) {
        MemberGoalSnapshotResult result = goalFacade.getMemberGoalSnapshot(new GoalCriteria.MemberGoal(userId));
        return ApiResponse.success(new GoalResponse.Goal(result.goal()));
    }

    @Override
    @GetMapping("/crews/{crewId}")
    public ApiResponse<GoalResponse.Goal> getCrewGoal(@PathVariable Long crewId,
                                                      @RequestHeader("X-USER-ID") Long userId) {
        CrewGoalSnapshotResult result = goalFacade.getCrewGoalSnapshot(new GoalCriteria.CrewGoal(crewId));
        return ApiResponse.success(new GoalResponse.Goal(result.goal()));
    }

    @Override
    @GetMapping("/me/last/achieve")
    public ApiResponse<GoalResponse.Achieve> getAchieve(@RequestHeader("X-USER-ID") Long userId) {
        MemberGoalSnapshotResult result = goalFacade.getLastWeekMemberGoalSnapshot(new GoalCriteria.MemberGoal(userId));
        return ApiResponse.success(new GoalResponse.Achieve(result.achieved()));
    }

    @Override
    @GetMapping("/crews/{crewId}/last/achieve")
    public ApiResponse<GoalResponse.Achieve> getCrewAchieve(@PathVariable Long crewId,
                                                            @RequestHeader("X-USER-ID") Long userId) {
        CrewGoalSnapshotResult result = goalFacade.getLastWeekCrewGoalSnapshot(new GoalCriteria.CrewGoal(crewId));
        return ApiResponse.success(new GoalResponse.Achieve(result.achieved()));
    }
}
