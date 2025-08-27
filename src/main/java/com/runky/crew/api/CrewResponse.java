package com.runky.crew.api;

import com.runky.crew.application.CrewResult;
import java.util.List;

public class CrewResponse {
    public record Create(
            Long crewId,
            String name,
            String code
    ) {
        public static Create from(CrewResult result) {
            return new Create(result.id(), result.name(), result.code());
        }
    }

    public record Join(
            Long crewId
    ) {
        public static Join from(CrewResult result) {
            return new Join(result.id());
        }
    }

    public record Cards(
            List<Card> crews
    ) {
        public static Cards from(List<CrewResult.Card> cards) {
            List<Card> crewCards = cards.stream()
                    .map(Card::from)
                    .toList();
            return new Cards(crewCards);
        }
    }

    public record Card(
            Long crewId,
            String name,
            Long memberCount,
            boolean isLeader,
            List<String> badgeImageUrls,
            boolean isRunning
    ) {
        public static Card from(CrewResult.Card card) {
            return new Card(
                    card.crewId(),
                    card.crewName(),
                    card.memberCount(),
                    card.isLeader(),
                    card.badgeImageUrls(),
                    card.isRunning()
            );
        }
    }

    public record Detail(
            Long crewId,
            String name,
            String leaderNickname,
            String notice,
            Long memberCount,
            String code
    ) {
        public static Detail from(CrewResult.Detail detail) {
            return new Detail(
                    detail.crewId(),
                    detail.name(),
                    detail.leaderNickname(),
                    detail.notice(),
                    detail.memberCount(),
                    detail.code()
            );
        }
    }

    public record Leave(
            Long crewId
    ) {
        public static Leave from(CrewResult.Leave result) {
            return new Leave(result.crewId());
        }
    }

    public record Members(List<Member> members) {

    }

    public record Member(
            Long memberId,
            String nickname,
            String character
    ) {

        public static Member from(CrewResult.CrewMember member) {
            return new Member(
                    member.memberId(),
                    member.nickname(),
                    member.character()
            );
        }
    }

    public record Notice(
            String notice
    ) {
    }

    public record Name(
            String name
    ) {
    }

    public record Disband(
            String name
    ) {
    }

    public record Delegate(
            Long leaderId,
            String leaderNickname
    ) {
    }

    public record Ban(
            Long targetId,
            String nickname
    ) {
    }

    private CrewResponse() {
    }
}
