package com.runky.goal.interfaces;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.goal.application.CrewGoalSnapshotResult;
import com.runky.goal.application.GoalCriteria;
import com.runky.goal.application.GoalCriteria.LastWeekClover;
import com.runky.goal.application.GoalFacade;
import com.runky.goal.application.MemberGoalResult;
import com.runky.goal.application.MemberGoalSnapshotResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController implements GoalApiSpec {

	private final GoalFacade goalFacade;

	@Override
	@PatchMapping("/me")
	public ApiResponse<GoalResponse.Goal> updateGoal(@RequestBody GoalRequest.Goal request,
		@AuthenticationPrincipal MemberPrincipal principal) {
		MemberGoalResult result = goalFacade.updateMemberGoal(
			new GoalCriteria.Update(principal.memberId(), request.goal()));
		return ApiResponse.success(new GoalResponse.Goal(result.goal()));
	}

	@Override
	@GetMapping("/me")
	public ApiResponse<GoalResponse.Goal> getGoal(@AuthenticationPrincipal MemberPrincipal principal) {
		MemberGoalSnapshotResult result = goalFacade.getMemberGoalSnapshot(
			new GoalCriteria.MemberGoal(principal.memberId()));
		return ApiResponse.success(new GoalResponse.Goal(result.goal()));
	}

	@Override
	@GetMapping("/crews/{crewId}")
	public ApiResponse<GoalResponse.Goal> getCrewGoal(@PathVariable Long crewId,
		@AuthenticationPrincipal MemberPrincipal principal) {
		CrewGoalSnapshotResult result = goalFacade.getCrewGoalSnapshot(new GoalCriteria.CrewGoal(crewId));
		return ApiResponse.success(new GoalResponse.Goal(result.goal()));
	}

	@Override
	@GetMapping("/me/last/achieve")
	public ApiResponse<GoalResponse.Achieve> getAchieve(@AuthenticationPrincipal MemberPrincipal principal) {
		MemberGoalSnapshotResult result = goalFacade.getLastWeekMemberGoalSnapshot(
			new GoalCriteria.MemberGoal(principal.memberId()));
		return ApiResponse.success(new GoalResponse.Achieve(result.achieved()));
	}

	@Override
	@GetMapping("/crews/{crewId}/last/achieve")
	public ApiResponse<GoalResponse.Achieve> getCrewAchieve(@PathVariable Long crewId,
		@AuthenticationPrincipal MemberPrincipal principal) {
		CrewGoalSnapshotResult result = goalFacade.getLastWeekCrewGoalSnapshot(new GoalCriteria.CrewGoal(crewId));
		return ApiResponse.success(new GoalResponse.Achieve(result.achieved()));
	}

	@Override
	@GetMapping("/me/last/clovers")
	public ApiResponse<GoalResponse.Clover> getMemberGoalClovers(@AuthenticationPrincipal MemberPrincipal principal) {
		MemberGoalSnapshotResult.Clover result =
			goalFacade.getLastWeekMemberGoalClover(new GoalCriteria.LastWeekClover(principal.memberId()));
		return ApiResponse.success(new GoalResponse.Clover(result.count()));
	}

	@Override
	@GetMapping("/crews/last/clovers")
	public ApiResponse<GoalResponse.Clover> getCrewGoalClovers(@AuthenticationPrincipal MemberPrincipal principal) {
		CrewGoalSnapshotResult.Clover result = goalFacade.getLastWeekCrewGoalClover(
			new LastWeekClover(principal.memberId()));
		return ApiResponse.success(new GoalResponse.Clover(result.count()));
	}
}
