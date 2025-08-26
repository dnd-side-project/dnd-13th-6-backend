package com.runky.member.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member API", description = "Runky Member API입니다.")
public interface MemberApiSpec {

	@Operation(
		summary = "내 정보 조회",
		description = "사용자의 정보를 조회합니다."
	)
	ApiResponse<MemberResponse.Detail> getMyInfo(
		@Parameter(hidden = true) MemberPrincipal requester
	);

	@Operation(
		summary = "닉네임 변경",
		description = "사용자의 닉네임을 변경합니다."
	)
	ApiResponse<MemberResponse.Nickname> changeNickname(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Schema(name = "닉네임 변경 요청", description = "변경할 닉네임") MemberRequest.Nickname request
	);

	@Operation(
		summary = "대표 뱃지 변경",
		description = "사용자의 대표 뱃지를 변경합니다."
	)
	ApiResponse<MemberResponse.Badge> changeBadge(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Schema(name = "캐릭터 변경 요청", description = "변경할 캐릭터 ID") MemberRequest.Badge request
	);

	@Operation(
		summary = "유저 대표 뱃지 조회",
		description = "유저의 대표 뱃지 정보를 조회합니다."
	)
	ApiResponse<MemberResponse.Badge> getMemberBadge(
		@Parameter(hidden = true) MemberPrincipal requester,
		Long targetId
	);
}
