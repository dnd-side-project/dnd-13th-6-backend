package com.runky.reward.domain;

import com.runky.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_badge")
public class UserBadge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "character_id", nullable = false)
    private Long badgeId;

    protected UserBadge() {
    }

    private UserBadge(Long id, Long userId, Long badgeId) {
        this.id = id;
        this.userId = userId;
        this.badgeId = badgeId;
    }

    public static UserBadge of(Long userId, Long badgeId) {
        return new UserBadge(null, userId, badgeId);
    }
}
