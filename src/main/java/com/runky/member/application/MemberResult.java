package com.runky.member.application;

import com.runky.member.domain.Member;
import com.runky.reward.domain.Badge;

public record MemberResult(
        Long id,
        String role,
        String nickname,
        String badgeUrl
) {
    public static MemberResult of(Member member, Badge badge) {
        return new MemberResult(
                member.getId(),
                member.getRole().name(),
                member.getNickname().value(),
                badge.getImageUrl()
        );
    }
}
