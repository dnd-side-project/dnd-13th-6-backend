package com.runky.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;

public interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	Optional<Crew> findByCode(Code code);

	@Query("SELECT c FROM Crew c JOIN c.members m WHERE m.memberId = :memberId AND (m.role = 'LEADER' OR m.role = 'MEMBER')")
	List<Crew> findCrewsByMemberId(Long memberId);

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
