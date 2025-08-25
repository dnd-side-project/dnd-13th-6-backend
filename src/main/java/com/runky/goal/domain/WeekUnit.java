package com.runky.goal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class WeekUnit {

    @Column(name = "iso_year", nullable = false)
    private int isoYear;

    @Column(name = "iso_week", nullable = false)
    private int isoWeek;

    protected WeekUnit() {
    }

    public WeekUnit(int isoYear, int isoWeek) {
        this.isoYear = isoYear;
        this.isoWeek = isoWeek;
    }

    public static WeekUnit from(LocalDate localDate) {
        WeekFields weekFields = WeekFields.ISO;
        int isoYear = localDate.get(weekFields.weekBasedYear());
        int isoWeek = localDate.get(weekFields.weekOfWeekBasedYear());
        return new WeekUnit(isoYear, isoWeek);
    }

    public int isoYear() {
        return isoYear;
    }

    public int isoWeek() {
        return isoWeek;
    }
}
