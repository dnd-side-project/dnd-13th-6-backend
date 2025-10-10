package com.runky.crew.infrastructure;

import com.runky.crew.domain.CrewMemberCount;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberCountJpaRepository extends JpaRepository<CrewMemberCount, Long> {
    Optional<CrewMemberCount> findByMemberId(Long memberId);

    List<CrewMemberCount> findByMemberIdIn(Set<Long> memberIds);

	void deleteByMemberId(Long memberId);
}
