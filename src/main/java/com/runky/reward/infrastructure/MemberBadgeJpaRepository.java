package com.runky.reward.infrastructure;

import com.runky.reward.domain.MemberBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBadgeJpaRepository extends JpaRepository<MemberBadge, Long> {

    boolean existsByMemberIdAndBadgeId(Long memberId, Long badgeId);
}
