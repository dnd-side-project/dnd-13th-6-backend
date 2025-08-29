package com.runky.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;

public interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	Optional<Crew> findByCode(Code code);

    @Query("""
    SELECT DISTINCT c
    FROM Crew c
    JOIN FETCH c.members
    WHERE c.id IN (
        SELECT c2.id
        FROM Crew c2
        JOIN c2.members m2
        WHERE m2.memberId = :memberId AND m2.role IN ('MEMBER', 'LEADER')
    )
""")
	List<Crew> findCrewsByMemberId(Long memberId);

    @Query("SELECT c FROM Crew c LEFT JOIN FETCH c.members WHERE c.id = :crewId")
    Optional<Crew> findCrewJoinFetch(Long crewId);

	@Query("""
			select distinct m2.memberId
			from CrewMember m2
			where m2.memberId <> :userId
			and m2.crew.id in (
			select m1.crew.id
			from CrewMember m1
			where m1.memberId = :userId
		)
		       """)
	List<Long> findAllCrewMembersOfUserWithoutUserId(Long userId);

	@Query("SELECT c FROM Crew c LEFT JOIN FETCH c.members")
	List<Crew> findAllCrewsJoinFetch();
}
