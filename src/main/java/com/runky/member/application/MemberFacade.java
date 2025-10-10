package com.runky.member.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.AuthTokenService;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewService;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.MemberService;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberFacade {

	private final AuthTokenService authTokenService;
	private final MemberService memberService;
	private final RewardService rewardService;
	private final CrewService crewService;

	@Transactional(readOnly = true)
	public MemberResult.WithBadge getMember(MemberCriteria.Get criteria) {
		Member member = memberService.getMember(new MemberCommand.Find(criteria.memberId()));
		Badge badge = rewardService.getBadge(new RewardCommand.Find(member.getBadgeId()));
		return MemberResult.WithBadge.of(member, badge);
	}

	@Transactional
	public MemberResult changeNickname(MemberCriteria.ChangeNickname criteria) {
		Member member = memberService.changeNickname(criteria.toCommand());
		return MemberResult.from(member);
	}

	@Transactional
	public MemberResult.WithBadge changeBadge(MemberCriteria.ChangeBadge criteria) {
		Badge badge = rewardService.getMemberBadge(
			new RewardCommand.FindMemberBadge(criteria.memberId(), criteria.badgeId()));
		Member member = memberService.changeBadge(new MemberCommand.ChangeBadge(criteria.memberId(), badge.getId()));
		return MemberResult.WithBadge.of(member, badge);
	}

	@Transactional
	public void deleteAccount(Long memberId) {
		authTokenService.delete(memberId);
		memberService.delete(new MemberCommand.DeleteMember(memberId));
		crewService.cleanUp(new CrewCommand.Clean(memberId));
	}
}
