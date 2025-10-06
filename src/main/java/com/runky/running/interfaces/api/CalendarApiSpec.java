package com.runky.running.interfaces.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

@Tag(name = "Calendar API", description = "Runky Calendar API입니다.")
public interface CalendarApiSpec {

    @Operation(
            summary = "주간 러닝 기록 조회",
            description = "특정 주(월~일)에 대한 사용자의 모든 런닝 기록을 조회합니다. 주의 시작일(월요일)을 기준으로 데이터를 반환합니다."
    )
    ApiResponse<CalendarResponse.Histories> getWeeklyHistories(@Parameter(hidden = true) MemberPrincipal requester,
                                                               LocalDate date);

    @Operation(
            summary = "월간 러닝 기록 조회",
            description = "특정 월에 대한 사용자의 모든 런닝 기록을 조회합니다. 월의 시작일(1일)을 기준으로 데이터를 반환합니다."
    )
    ApiResponse<CalendarResponse.Histories> getMonthlyHistories(@Parameter(hidden = true) MemberPrincipal requester,
                                                                int year, int month);
}
