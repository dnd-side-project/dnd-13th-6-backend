package com.runky.crew.application;

import com.runky.crew.domain.Crew;
import java.math.BigDecimal;
import java.util.List;

public record CrewResult(
        Long id,
        String name,
        String code,
        Long leaderId,
        String notice,
        Long memberCount
) {
    public static CrewResult from(Crew crew) {
        return new CrewResult(
                crew.getId(),
                crew.getName(),
                crew.getCode().value(),
                crew.getLeaderId(),
                crew.getNotice(),
                crew.getActiveMemberCount()
        );
    }

    public record Card(
            Long crewId,
            String crewName,
            Long memberCount,
            boolean isLeader,
            List<String> badgeImageUrls,
            boolean isRunning
    ) {
    }

    public record Detail(
            Long crewId,
            String name,
            String leaderNickname,
            String notice,
            Long memberCount,
            BigDecimal goal,
            String code
    ) {
    }

    public record CrewMember(
            Long memberId,
            String nickname,
            String character
    ) {
    }

    public record Leave(
            Long crewId,
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
}
