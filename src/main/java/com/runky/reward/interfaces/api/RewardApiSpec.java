package com.runky.reward.interfaces.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.reward.interfaces.api.RewardResponse.Gotcha;
import com.runky.reward.interfaces.api.RewardResponse.Images;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reward API", description = "Runky Reward API입니다.")
public interface RewardApiSpec {

	@Operation(
		summary = "내 뱃지 목록 조회",
		description = "내가 보유한 뱃지 목록을 조회합니다."
	)
	ApiResponse<Images> getBadges(
		@Parameter(hidden = true) MemberPrincipal requester
	);

	@Operation(
		summary = "뱃지 뽑기",
		description = "클로버를 사용해 뱃지를 뽑습니다."
	)
	ApiResponse<Gotcha> drawBadge(
		@Parameter(hidden = true) MemberPrincipal requester
	);

	@Operation(
		summary = "클로버 개수 조회",
		description = "사용자의 클로버 개수를 조회합니다."
	)
	ApiResponse<RewardResponse.Clover> getCloverCount(
		@Parameter(hidden = true) MemberPrincipal requester
	);
}
