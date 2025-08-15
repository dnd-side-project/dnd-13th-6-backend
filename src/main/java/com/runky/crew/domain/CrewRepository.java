package com.runky.crew.domain;

import java.util.List;
import java.util.Optional;

public interface CrewRepository {

    Optional<Crew> findById(Long crewId);

    Optional<Crew> findCrewByCode(Code code);

    List<Crew> findCrewsByMemberId(Long memberId);

    Optional<CrewMember> findByCrewAndMember(Long crewId, Long memberId);

    List<CrewMember> findCrewMemberOfUser(Long memberId);

    Optional<CrewMemberCount> findCountByMemberId(Long memberId);

    Crew save(Crew crew);

    CrewMemberCount save(CrewMemberCount crewMemberCount);
}
