package com.runky.member.api;

public class MemberResponse {

    public record Detail(
            Long userId,
            String nickname,
            String badgeUrl
    ) {
    }

    public record Nickname(
            Long userId,
            String nickname
    ) {
    }

    public record Badge(
            Long userId,
            String badgeImageUrl
    ) {
    }

    private MemberResponse() {
    }
}
