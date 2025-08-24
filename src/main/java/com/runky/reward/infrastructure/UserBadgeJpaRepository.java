package com.runky.reward.infrastructure;

import com.runky.reward.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeJpaRepository extends JpaRepository<UserBadge, Long> {

}
