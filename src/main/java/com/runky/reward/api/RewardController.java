package com.runky.reward.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.reward.api.RewardResponse.Gotcha;
import com.runky.reward.application.RewardCriteria;
import com.runky.reward.application.RewardFacade;
import com.runky.reward.application.RewardResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardController implements RewardApiSpec {

	private final RewardFacade rewardFacade;

	@Override
	@GetMapping("/badges")
	public ApiResponse<RewardResponse.Images> getBadges(@AuthenticationPrincipal MemberPrincipal requester) {
		List<RewardResult.Badge> results = rewardFacade.getMyBadges(new RewardCriteria.Find(requester.memberId()));
		RewardResponse.Images response = new RewardResponse.Images(results.stream()
			.map(result -> new RewardResponse.Image(result.badgeId(), result.ImageUrl()))
			.toList());
		return ApiResponse.success(response);
	}

	@Override
	@PatchMapping("/gotcha")
	public ApiResponse<RewardResponse.Gotcha> drawBadge(@AuthenticationPrincipal MemberPrincipal requester) {
		RewardResult.Gotcha result = rewardFacade.gotcha(new RewardCriteria.Gotcha(requester.memberId()));
		Gotcha response = new Gotcha(result.id(), result.imageUrl(), result.name());
		return ApiResponse.success(response);
	}

	@Override
	@GetMapping("/clovers")
	public ApiResponse<RewardResponse.Clover> getCloverCount(@AuthenticationPrincipal MemberPrincipal requester) {
		RewardResult.Clover result = rewardFacade.getClover(new RewardCriteria.Find(requester.memberId()));
		return ApiResponse.success(new RewardResponse.Clover(result.count()));
	}
}
