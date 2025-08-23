package com.runky.member.api;

import com.runky.global.response.ApiResponse;
import com.runky.member.application.MemberCriteria;
import com.runky.member.application.MemberFacade;
import com.runky.member.application.MemberResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/members/me")
@RequiredArgsConstructor
public class MemberController implements MemberApiSpec {

    private final MemberFacade memberFacade;

    @Override
    @GetMapping
    public ApiResponse<MemberResponse.Detail> getMyInfo(@RequestHeader("X-USER-ID") Long userId) {
        MemberResult result = memberFacade.getMember(new MemberCriteria.Get(userId));
        return ApiResponse.success(new MemberResponse.Detail(result.id(), result.nickname(), result.badgeUrl()));
    }

    @Override
    @PatchMapping("/nickname")
    public ApiResponse<MemberResponse.Nickname> changeNickname(@RequestBody MemberRequest.Nickname request,
                                                               @RequestHeader("X-USER-ID") Long userId) {
        return ApiResponse.success(new MemberResponse.Nickname(5L, "nickname"));
    }

    @Override
    @PatchMapping("/character")
    public ApiResponse<MemberResponse.Character> changeCharacter(@RequestBody MemberRequest.Character request,
                                                                 @RequestHeader("X-USER-ID") Long userId) {
        return ApiResponse.success(new MemberResponse.Character(5L, "badgeUrl"));
    }
}
