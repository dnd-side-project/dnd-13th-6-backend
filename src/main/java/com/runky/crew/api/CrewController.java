package com.runky.crew.api;

import com.runky.crew.api.CrewResponse.Related.RunningMember;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.crew.application.CrewCriteria;
import com.runky.crew.application.CrewFacade;
import com.runky.crew.application.CrewResult;
import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crews")
@RequiredArgsConstructor
public class CrewController implements CrewApiSpec {
	private final CrewFacade crewFacade;

	@Override
	@PostMapping
	public ApiResponse<CrewResponse.Create> createCrew(
		@AuthenticationPrincipal MemberPrincipal requester,
		@Valid @RequestBody CrewRequest.Create request
	) {
		CrewResult result = crewFacade.create(new CrewCriteria.Create(requester.memberId(), request.name()));
		return ApiResponse.success(CrewResponse.Create.from(result));
	}

	@Override
	@PostMapping("/join")
	public ApiResponse<CrewResponse.Join> joinCrew(
		@AuthenticationPrincipal MemberPrincipal requester,
		@Valid @RequestBody CrewRequest.Join request
	) {
		CrewResult result = crewFacade.join(new CrewCriteria.Join(requester.memberId(), request.code()));
		return ApiResponse.success(CrewResponse.Join.from(result));
	}

	@Override
	@GetMapping
	public ApiResponse<CrewResponse.Cards> getCrews(@AuthenticationPrincipal MemberPrincipal requester) {
		List<CrewResult.Card> cards = crewFacade.getCrews(requester.memberId());
		return ApiResponse.success(CrewResponse.Cards.from(cards));
	}

	@Override
	@GetMapping("/{crewId}")
	public ApiResponse<CrewResponse.Detail> getCrew(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long crewId
	) {
		CrewResult.Detail result = crewFacade.getCrew(new CrewCriteria.Detail(crewId, requester.memberId()));
		return ApiResponse.success(CrewResponse.Detail.from(result));
	}

	@Override
	@DeleteMapping("/{crewId}/members/me")
	public ApiResponse<CrewResponse.Leave> leaveCrew(
		@AuthenticationPrincipal MemberPrincipal requester,
		@Valid @RequestBody CrewRequest.Leave request,
		@PathVariable Long crewId
	) {
		CrewResult.Leave result = crewFacade.leaveCrew(
			new CrewCriteria.Leave(crewId, requester.memberId(), request.newLeaderId()));
		return ApiResponse.success(CrewResponse.Leave.from(result));
	}

	@Override
	@GetMapping("/{crewId}/members")
	public ApiResponse<CrewResponse.Members> getCrewMembers(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long crewId
	) {
		List<CrewResult.CrewMember> results = crewFacade.getCrewMembers(
			new CrewCriteria.Members(crewId, requester.memberId()));
		List<CrewResponse.Member> members = results.stream()
			.map(CrewResponse.Member::from)
			.toList();
		return ApiResponse.success(new CrewResponse.Members(members));
	}

	@Override
	@PatchMapping("/{crewId}/notice")
	public ApiResponse<CrewResponse.Notice> updateNotice(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody CrewRequest.Notice request,
		@PathVariable Long crewId
	) {
		CrewResult result = crewFacade.updateNotice(
			new CrewCriteria.UpdateNotice(crewId, requester.memberId(), request.notice()));
		return ApiResponse.success(new CrewResponse.Notice(result.notice()));
	}

	@Override
	@PatchMapping("/{crewId}/name")
	public ApiResponse<CrewResponse.Name> updateName(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody CrewRequest.Name request,
		@PathVariable Long crewId
	) {
		CrewResult result = crewFacade.updateName(
			new CrewCriteria.UpdateName(crewId, requester.memberId(), request.name()));
		return ApiResponse.success(new CrewResponse.Name(result.name()));
	}

	@Override
	@DeleteMapping("/{crewId}")
	public ApiResponse<CrewResponse.Disband> disbandCrew(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long crewId) {
		CrewResult result = crewFacade.disband(new CrewCriteria.Disband(crewId, requester.memberId()));
		return ApiResponse.success(new CrewResponse.Disband(result.name()));
	}

	@Override
	@PatchMapping("/{crewId}/leader")
	public ApiResponse<CrewResponse.Delegate> delegateLeader(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody CrewRequest.Delegate request,
		@PathVariable Long crewId
	) {
		CrewResult.Delegate result = crewFacade.delegateLeader(
			new CrewCriteria.Delegate(crewId, requester.memberId(), request.newLeaderId()));
		return ApiResponse.success(new CrewResponse.Delegate(result.leaderId(), result.leaderNickname()));
	}

	@Override
	@DeleteMapping("/{crewId}/members/{memberId}")
	public ApiResponse<CrewResponse.Ban> banMember(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable("crewId") Long crewId,
		@PathVariable("memberId") Long targetId
	) {
		CrewResult.Ban result = crewFacade.banMember(new CrewCriteria.Ban(crewId, requester.memberId(), targetId));
		return ApiResponse.success(new CrewResponse.Ban(result.targetId(), result.nickname()));
	}

    @Override
    @GetMapping("/members/running")
    public ApiResponse<CrewResponse.Related> getRunningRelatedMembers(
            @AuthenticationPrincipal MemberPrincipal requester) {
        List<CrewResult.RelatedRunningMember> results =
                crewFacade.getRelatedRunningMember(new CrewCriteria.RelatedRunningMember(requester.memberId()));

        List<RunningMember> runningMembers = results.stream()
                .map(result -> new CrewResponse.Related.RunningMember(result.nickname(), result.badgeImageUrl()))
                .toList();

        return ApiResponse.success(new CrewResponse.Related(runningMembers));
    }
}
