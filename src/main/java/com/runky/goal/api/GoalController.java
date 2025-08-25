package com.runky.goal.api;

import com.runky.global.response.ApiResponse;
import com.runky.goal.api.GoalResponse;
import com.runky.goal.application.GoalCriteria;
import com.runky.goal.application.GoalFacade;
import com.runky.goal.application.MemberGoalSnapshotResult;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
        return ApiResponse.success(new GoalResponse.Goal(request.goal()));
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<GoalResponse.Goal> getGoal(@RequestHeader("X-USER-ID") Long userId) {
        MemberGoalSnapshotResult result = goalFacade.getMemberGoalSnapshot(new GoalCriteria.MemberGoal(userId));
        return ApiResponse.success(new GoalResponse.Goal(result.goal()));
    }

    @Override
    @GetMapping("/crews/{crewId}")
    public ApiResponse<GoalResponse.Goal> getGroupGoal(@RequestHeader("X-USER-ID") Long userId) {
        return ApiResponse.success(new GoalResponse.Goal(new BigDecimal("43.2")));
    }
}
