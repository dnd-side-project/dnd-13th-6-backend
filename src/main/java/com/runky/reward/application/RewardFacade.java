package com.runky.reward.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.runky.reward.domain.Badge;
import com.runky.reward.domain.Clover;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RewardFacade {

	private final RewardService rewardService;

	public List<RewardResult.Badge> getMyBadges(RewardCriteria.Find criteria) {
		List<Badge> badges = rewardService.getBadges(new RewardCommand.GetBadges(criteria.userId()));
		return badges.stream()
			.map(badge -> new RewardResult.Badge(badge.getId(), badge.getImageUrl(), badge.getName()))
			.toList();
	}

	public RewardResult.Clover getClover(RewardCriteria.Find criteria) {
		Clover clover = rewardService.getClover(new RewardCommand.GetClover(criteria.userId()));
		return new RewardResult.Clover(clover.getUserId(), clover.getCount());
	}

	public RewardResult.Gotcha gotcha(RewardCriteria.Gotcha criteria) {
		Badge badge = rewardService.gotcha(new RewardCommand.Gotcha(criteria.userId()));
		return new RewardResult.Gotcha(badge.getId(), badge.getName(), badge.getImageUrl());
	}
}
