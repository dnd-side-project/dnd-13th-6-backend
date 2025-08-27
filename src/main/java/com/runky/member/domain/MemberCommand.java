package com.runky.member.domain;

import java.util.Set;

public class MemberCommand {

    public record RegisterFromExternal(String provider, String providerId, String nickname) {
    }

    public record Find(
            Long memberId
    ) {
    }

    public record ChangeNickname(
            Long memberId,
            String nickname
    ) {
    }

    public record ChangeBadge(
            Long memberId,
            Long badgeId
    ) {
    }

    public record GetMembers(
            Set<Long> memberIds
    ) {
    }
}
