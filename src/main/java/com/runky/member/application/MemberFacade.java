package com.runky.member.application;

import com.runky.member.domain.Member;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.MemberService;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberService memberService;
    private final RewardService rewardService;

    public MemberResult.WithBadge getMember(MemberCriteria.Get criteria) {
        Member member = memberService.getMember(new MemberCommand.Find(criteria.memberId()));
        Badge badge = rewardService.getBadge(new RewardCommand.Find(member.getId()));
        return MemberResult.WithBadge.of(member, badge);
    }

    public MemberResult changeNickname(MemberCriteria.ChangeNickname criteria) {
        Member member = memberService.changeNickname(criteria.toCommand());
        return MemberResult.from(member);
    }

    public MemberResult.WithBadge changeBadge(MemberCriteria.ChangeBadge criteria) {
        Badge badge = rewardService.getMemberBadge(
                new RewardCommand.FindMemberBadge(criteria.memberId(), criteria.badgeId()));
        Member member = memberService.changeBadge(new MemberCommand.ChangeBadge(criteria.memberId(), badge.getId()));
        return MemberResult.WithBadge.of(member, badge);
    }
}
