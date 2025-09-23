package com.runky.goal.interfaces;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

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
		MemberPrincipal principal
	);

	@Operation(
		summary = "개인 목표 조회",
		description = "사용자의 개인 목표를 조회합니다."
	)
	ApiResponse<GoalResponse.Goal> getGoal(
		MemberPrincipal principal
	);

	@Operation(
		summary = "이번주 크루 목표 조회",
		description = "크루의 목표를 조회합니다."
	)
	ApiResponse<GoalResponse.Goal> getCrewGoal(
		@Schema(name = "크루 ID", description = "목표를 조회할 크루 ID") Long crewId,
		MemberPrincipal principal
	);

	@Operation(
		summary = "지난주 개인 목표 달성 여부 조회",
		description = "사용자의 지난주 개인 목표 달성 여부를 조회합니다."
	)
	ApiResponse<GoalResponse.Achieve> getAchieve(
		MemberPrincipal principal
	);

	@Operation(
		summary = "지난주 크루 목표 달성 여부 조회",
		description = "크루의 지난주 목표 달성 여부를 조회합니다."
	)
	ApiResponse<GoalResponse.Achieve> getCrewAchieve(
		@Schema(name = "크루 ID", description = "목표 달성 여부를 조회할 크루 ID") Long crewId,
		MemberPrincipal principal

	);

	@Operation(
		summary = "저번주 개인 목표 클로버 획득 개수 조회",
		description = "사용자의 저번주 개인 목표 클로버 획득 개수를 조회합니다."
	)
	ApiResponse<GoalResponse.Clover> getMemberGoalClovers(
		MemberPrincipal principal
	);

	@Operation(
		summary = "저번주 크루 목표 클로버 획득 개수 조회",
		description = "크루의 저번주 목표 클로버 획득 개수를 조회합니다."
	)
	ApiResponse<GoalResponse.Clover> getCrewGoalClovers(
		MemberPrincipal principal
	);
}
