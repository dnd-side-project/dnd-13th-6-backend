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
import java.util.List;
import lombok.Getter;

@Entity
@Getter
@Table(name = "crew_goal_snapshot")
public class CrewGoalSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private Long crewId;

    @Embedded
    private Goal goal;

    @Column(name = "achieved", nullable = false)
    private Boolean achieved;

    @Embedded
    private WeekUnit weekUnit;

    protected CrewGoalSnapshot() {
    }

    public CrewGoalSnapshot(Long crewId, Goal goal, Boolean achieved, WeekUnit weekUnit) {
        this.crewId = crewId;
        this.goal = goal;
        this.achieved = achieved;
        this.weekUnit = weekUnit;
    }

    public static CrewGoalSnapshot of(List<MemberGoalSnapshot> memberGoalSnapshots, Long crewId, LocalDate date) {
        BigDecimal crewGoal = memberGoalSnapshots.stream()
                .map(snapshot -> snapshot.getGoal().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CrewGoalSnapshot(crewId, new Goal(crewGoal), false, WeekUnit.from(date));
    }

    public static CrewGoalSnapshot empty(Long crewId, LocalDate date) {
        return new CrewGoalSnapshot(crewId, new Goal(BigDecimal.ZERO), false, WeekUnit.from(date));
    }

    public void achieve() {
        this.achieved = true;
    }
}
