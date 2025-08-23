package com.runky.reward.infrastructure;

import com.runky.reward.domain.Badge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BadgeJpaRepository extends JpaRepository<Badge, Long> {

    @Query("SELECT b FROM Badge b JOIN MemberBadge mb ON b.id = mb.badgeId WHERE mb.memberId = :memberId")
    List<Badge> findBadgesOf(Long memberId);
}
