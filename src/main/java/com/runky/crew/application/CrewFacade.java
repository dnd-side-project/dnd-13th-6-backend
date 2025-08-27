package com.runky.crew.application;

import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewLeaderService;
import com.runky.crew.domain.CrewMember;
import com.runky.crew.domain.CrewService;
import com.runky.goal.domain.GoalService;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.MemberService;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;
import com.runky.running.domain.RunningService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CrewFacade {
    private final CrewService crewService;
    private final MemberService memberService;
    private final CrewLeaderService crewLeaderService;
    private final RewardService rewardService;
    private final RunningService runningService;
    private final GoalService goalService;

    public CrewResult create(CrewCriteria.Create criteria) {
        Crew crew = crewService.create(criteria.toCommand());
        return CrewResult.from(crew);
    }

    public CrewResult join(CrewCriteria.Join criteria) {
        Crew crew = crewService.join(criteria.toCommand());
        return CrewResult.from(crew);
    }

    @Transactional(readOnly = true)
    public List<CrewResult.Card> getCrews(Long userId) {
        List<Crew> crews = crewService.getCrewsOfUser(userId);

        List<CrewResult.Card> cards = new ArrayList<>();
        for (Crew crew : crews) {
            Set<Long> crewMemberIds = crew.getActiveMembers().stream()
                    .map(CrewMember::getMemberId)
                    .collect(Collectors.toSet());

            List<Member> members = memberService.getMembers(new MemberCommand.GetMembers(crewMemberIds));

            List<String> imageUrls = members.stream()
                    .map(Member::getBadgeId)
                    .map(badgeId -> {
                        Badge badge = rewardService.getBadge(new RewardCommand.Find(badgeId));
                        return badge.getImageUrl();
                    })
                    .toList();

            boolean isRunning = crewMemberIds.stream()
                    .anyMatch(runningService::getRunnerStatus);

            cards.add(new CrewResult.Card(crew.getId(), crew.getName(), crew.getActiveMemberCount(),
                    crew.getLeaderId().equals(userId), imageUrls, isRunning));
        }

        return cards;
    }

    public CrewResult.Detail getCrew(CrewCriteria.Detail criteria) {
        Crew crew = crewService.getCrew(criteria.toCrewCommand());

        Member leader = memberService.getMember(new MemberCommand.Find(crew.getLeaderId()));

        return new CrewResult.Detail(crew.getId(), crew.getName(), leader.getNickname().value(), crew.getNotice(),
                crew.getActiveMemberCount(), crew.getCode().value());
    }

    public List<CrewResult.CrewMember> getCrewMembers(CrewCriteria.Members criteria) {
        List<CrewMember> members = crewService.getCrewMembers(criteria.toCommand());
        // TODO 크루원 캐릭터 이미지 + 닉네임 불러오는 작업 추가
        return members.stream()
                .map(member -> new CrewResult.CrewMember(member.getId(), "nickname", "runky/1.png"))
                .toList();
    }

    public CrewResult.Leave leaveCrew(CrewCriteria.Leave criteria) {
        Crew crew = crewService.leave(criteria.toCommand());
        return new CrewResult.Leave(crew.getId(), crew.getName());
    }

    public CrewResult updateNotice(CrewCriteria.UpdateNotice criteria) {
        Crew crew = crewLeaderService.updateNotice(criteria.toCommand());
        return CrewResult.from(crew);
    }

    public CrewResult updateName(CrewCriteria.UpdateName criteria) {
        Crew crew = crewLeaderService.updateName(criteria.toCommand());
        return CrewResult.from(crew);
    }

    public CrewResult disband(CrewCriteria.Disband criteria) {
        Crew crew = crewLeaderService.disband(criteria.toCommand());
        return CrewResult.from(crew);
    }

    public CrewResult.Delegate delegateLeader(CrewCriteria.Delegate criteria) {
        Crew crew = crewLeaderService.delegateLeader(criteria.toCommand());

        // TODO 크루 리더의 닉네임을 가져오는 작업 추가 : UserService
        String leaderNickname = "Leader Nickname";

        return new CrewResult.Delegate(crew.getLeaderId(), leaderNickname);
    }

    public CrewResult.Ban banMember(CrewCriteria.Ban criteria) {
        CrewMember bannedMember = crewLeaderService.ban(criteria.toCommand());

        // TODO 크루 리더의 닉네임을 가져오는 작업 추가 : UserService
        String bannedNickname = "Banned Nickname";

        return new CrewResult.Ban(bannedMember.getMemberId(), bannedNickname);
    }
}
