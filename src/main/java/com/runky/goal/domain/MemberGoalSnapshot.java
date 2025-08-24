package com.runky.goal.domain;

import com.runky.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Getter
@Table(name = "member_goal_snapshot")
public class MemberGoalSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Embedded
    private Goal goal;

    @Column(name = "achieved", nullable = false)
    private Boolean achieved;

    @Embedded
    private WeekUnit weekUnit;

    protected MemberGoalSnapshot() {
    }

    public MemberGoalSnapshot(Long memberId, Goal goal, Boolean achieved, LocalDate localDate) {
        this.memberId = memberId;
        this.goal = goal;
        this.achieved = achieved;
        this.weekUnit = WeekUnit.from(localDate);
    }
}
