package com.runky.reward.domain;

import com.runky.global.entity.BaseTimeEntity;
import com.runky.goal.domain.WeekUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "member_clover_history")
public class MemberCloverHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Embedded
    private WeekUnit weekUnit;

    @Column(name = "amount", nullable = false)
    private Long amount;

    protected MemberCloverHistory() {
    }

    public MemberCloverHistory(Long memberId, WeekUnit weekUnit, Long amount) {
        this.memberId = memberId;
        this.weekUnit = weekUnit;
        this.amount = amount;
    }
}
