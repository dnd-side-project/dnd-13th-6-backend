package com.runky.goal.domain;

import com.runky.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Getter
@Table(name = "member_goal")
public class MemberGoal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Embedded
    private Goal goal;

    protected MemberGoal() {
    }

    private MemberGoal(Long memberId, Goal goal) {
        this.memberId = memberId;
        this.goal = goal;
    }

    public static MemberGoal from(Long memberId) {
        return new MemberGoal(memberId, new Goal(BigDecimal.ZERO));
    }

    public void updateGoal(BigDecimal newGoal) {
        this.goal = new Goal(newGoal);
    }

    public MemberGoalSnapshot createSnapshot(LocalDate date) {
        return new MemberGoalSnapshot(this.memberId, this.goal, false, date);
    }
}
