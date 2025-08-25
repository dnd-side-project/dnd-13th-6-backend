package com.runky.crew.infrastructure;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CrewJpaRepository extends JpaRepository<Crew, Long> {

    Optional<Crew> findByCode(Code code);

    @Query("SELECT c FROM Crew c JOIN c.members m WHERE m.memberId = :memberId AND (m.role = 'LEADER' OR m.role = 'MEMBER')")
    List<Crew> findCrewsByMemberId(Long memberId);

    @Query("SELECT c FROM Crew c LEFT JOIN FETCH c.members")
    List<Crew> findAllCrewsJoinFetch();
}
