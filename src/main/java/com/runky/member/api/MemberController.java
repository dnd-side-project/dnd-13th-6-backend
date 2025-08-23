package com.runky.member.api;

import com.runky.global.response.ApiResponse;
import com.runky.member.application.MemberCriteria;
import com.runky.member.application.MemberFacade;
import com.runky.member.application.MemberResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController implements MemberApiSpec {

    private final MemberFacade memberFacade;

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberResponse.Detail> getMyInfo(@RequestHeader("X-USER-ID") Long userId) {
        MemberResult.WithBadge result = memberFacade.getMember(new MemberCriteria.Get(userId));
        return ApiResponse.success(new MemberResponse.Detail(result.id(), result.nickname(), result.badgeImageUrl()));
    }

    @Override
    @PatchMapping("/me/nickname")
    public ApiResponse<MemberResponse.Nickname> changeNickname(@RequestBody MemberRequest.Nickname request,
                                                               @RequestHeader("X-USER-ID") Long userId) {
        MemberResult result = memberFacade.changeNickname(
                new MemberCriteria.ChangeNickname(userId, request.nickname()));
        return ApiResponse.success(new MemberResponse.Nickname(result.id(), result.nickname()));
    }

    @Override
    @PatchMapping("/me/badge")
    public ApiResponse<MemberResponse.Badge> changeNickname(@RequestBody MemberRequest.Badge request,
                                                            @RequestHeader("X-USER-ID") Long userId) {
        MemberResult.WithBadge result = memberFacade.changeBadge(
                new MemberCriteria.ChangeBadge(userId, request.badgeId()));
        return ApiResponse.success(new MemberResponse.Badge(result.id(), result.badgeImageUrl()));
    }

    @Override
    @GetMapping("/{memberId}/badge")
    public ApiResponse<MemberResponse.Badge> getMemberBadge(@PathVariable(value = "memberId") Long targetId,
                                                            @RequestHeader("X-USER-ID") Long userId) {
        MemberResult.WithBadge result = memberFacade.getMember(new MemberCriteria.Get(targetId));
        return ApiResponse.success(new MemberResponse.Badge(result.id(), result.badgeImageUrl()));
    }
}
