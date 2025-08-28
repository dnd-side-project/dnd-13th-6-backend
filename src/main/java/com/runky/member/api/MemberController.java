package com.runky.member.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.member.application.MemberCriteria;
import com.runky.member.application.MemberFacade;
import com.runky.member.application.MemberResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController implements MemberApiSpec {

	private final MemberFacade memberFacade;

	@Override
	@GetMapping("/me")
	public ApiResponse<MemberResponse.Detail> getMyInfo(@AuthenticationPrincipal MemberPrincipal requester) {
		MemberResult.WithBadge result = memberFacade.getMember(new MemberCriteria.Get(requester.memberId()));
		return ApiResponse.success(
			new MemberResponse.Detail(result.id(), result.nickname(), result.badgeId(), result.badgeImageUrl()));
	}

	@Override
	@PatchMapping("/me/nickname")
	public ApiResponse<MemberResponse.Nickname> changeNickname(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody MemberRequest.Nickname request
	) {
		MemberResult result = memberFacade.changeNickname(
			new MemberCriteria.ChangeNickname(requester.memberId(), request.nickname()));
		return ApiResponse.success(new MemberResponse.Nickname(result.id(), result.nickname()));
	}

	@Override
	@PatchMapping("/me/badge")
	public ApiResponse<MemberResponse.Badge> changeBadge(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody MemberRequest.Badge request
	) {
		MemberResult.WithBadge result = memberFacade.changeBadge(
			new MemberCriteria.ChangeBadge(requester.memberId(), request.badgeId()));
		return ApiResponse.success(new MemberResponse.Badge(result.id(), result.badgeId(), result.badgeImageUrl()));
	}

	@Override
	@GetMapping("/{memberId}/badge")
	public ApiResponse<MemberResponse.Badge> getMemberBadge(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable(value = "memberId") Long targetId) {
		MemberResult.WithBadge result = memberFacade.getMember(new MemberCriteria.Get(targetId));
		return ApiResponse.success(new MemberResponse.Badge(result.id(), result.badgeId(), result.badgeImageUrl()));
	}
}
