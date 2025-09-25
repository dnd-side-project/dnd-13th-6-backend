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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(name = "crew_clover_history", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_crew", columnNames = {"crew_id", "iso_year", "iso_week"})
})
public class CrewCloverHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private Long crewId;

    @Embedded
    private WeekUnit weekUnit;

    @Column(name = "amount", nullable = false)
    private Long amount;

    protected CrewCloverHistory() {
    }

    public CrewCloverHistory(Long crewId, WeekUnit weekUnit, Long amount) {
        this.crewId = crewId;
        this.weekUnit = weekUnit;
        this.amount = amount;
    }
}
