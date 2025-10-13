package com.runky.crew.infrastructure;

import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {
    Optional<CrewMember> findByCrewIdAndMemberId(Long crewId, Long memberId);

    List<CrewMember> findByMemberId(Long memberId);

    @Query("""
    select distinct m
    from CrewMember m
    where m.crew.id in (
        select m2.crew.id
        from CrewMember m2
        where m2.memberId = :memberId
          and (m2.role = com.runky.crew.domain.CrewMember.Role.LEADER or m2.role = com.runky.crew.domain.CrewMember.Role.MEMBER)
    )
    and (m.role = com.runky.crew.domain.CrewMember.Role.LEADER or m.role = com.runky.crew.domain.CrewMember.Role.MEMBER)
""")
    List<CrewMember> findRelatedCrewMembers(Long memberId);

	void deleteByMemberId(Long memberId);

	void deleteByCrew(Crew crew);
}
