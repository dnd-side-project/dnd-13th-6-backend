package com.runky.member.api;

public class MemberRequest {

    public record Nickname(
            String nickname
    ) {
    }

    public record Badge(
            Long badgeId
    ) {
    }

    private MemberRequest() {
    }
}
