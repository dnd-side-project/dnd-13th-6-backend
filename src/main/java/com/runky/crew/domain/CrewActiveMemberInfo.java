package com.runky.crew.domain;

import java.util.Set;

public record CrewActiveMemberInfo(
        Long crewId,
        Set<Long> memberIds
) {
}
