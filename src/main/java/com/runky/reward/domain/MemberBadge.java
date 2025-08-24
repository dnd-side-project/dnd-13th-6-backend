package com.runky.reward.domain;

import com.runky.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "member_badge")
public class MemberBadge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;

    protected MemberBadge() {
    }

    private MemberBadge(Long id, Long memberId, Long badgeId) {
        this.id = id;
        this.memberId = memberId;
        this.badgeId = badgeId;
    }

    public static MemberBadge of(Long userId, Long badgeId) {
        return new MemberBadge(null, userId, badgeId);
    }
}
