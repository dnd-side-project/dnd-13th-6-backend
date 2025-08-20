package com.runky.goal.api;

import com.runky.global.response.ApiResponse;
import com.runky.goal.api.GoalResponse;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoalController implements GoalApiSpec {
    @Override
    public ApiResponse<GoalResponse.Goal> updateGoal(@RequestBody GoalRequest.Goal request,
                                                     @RequestHeader("X-USER-ID") Long userId) {
        return ApiResponse.success(new GoalResponse.Goal(request.goal()));
    }

    @Override
    public ApiResponse<GoalResponse.Goal> getGoal(@RequestHeader("X-USER-ID") Long userId) {
        return ApiResponse.success(new GoalResponse.Goal(new BigDecimal("14.5")));
    }

    @Override
    public ApiResponse<GoalResponse.Goal> getGroupGoal(@RequestHeader("X-USER-ID") Long userId) {
        return ApiResponse.success(new GoalResponse.Goal(new BigDecimal("43.2")));
    }
}
