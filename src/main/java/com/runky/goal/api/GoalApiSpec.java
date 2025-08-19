package com.runky.goal.api;

import com.runky.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Goal API", description = "Runky Goal API입니다.")
public interface GoalApiSpec {

    @Operation(
            summary = "개인 목표 설정",
            description = "사용자의 개인 목표를 설정합니다."
    )
    ApiResponse<GoalResponse.Goal> updateGoal(
            @Schema(name = "목표 변경 요청", description = "변경할 목표 거리") GoalRequest.Goal goalRequest,
            @Schema(name = "사용자 ID", description = "X-USER-ID로 로그인 대체") Long userId
    );

    @Operation(
            summary = "개인 목표 조회",
            description = "사용자의 개인 목표를 조회합니다."
    )
    ApiResponse<GoalResponse.Goal> getGoal(
            @Schema(name = "사용자 ID", description = "X-USER-ID로 로그인 대체") Long userId
    );

    @Operation(
            summary = "그룹 목표 조회",
            description = "사용자가 속한 그룹의 목표를 조회합니다."
    )
    ApiResponse<GoalResponse.Goal> getGroupGoal(
            @Schema(name = "사용자 ID", description = "X-USER-ID로 로그인 대체") Long userId
    );
}
