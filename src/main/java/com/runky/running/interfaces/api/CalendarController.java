package com.runky.running.interfaces.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.running.application.RunningCriteria;
import com.runky.running.application.RunningFacade;
import com.runky.running.application.RunningResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController implements CalendarApiSpec {
    private final RunningFacade runningFacade;

    @Override
    @GetMapping("/weekly")
    public ApiResponse<CalendarResponse.Weekly> getWeeklyHistories(@AuthenticationPrincipal MemberPrincipal requester,
                                                                   @RequestParam("date") LocalDate date) {
        List<RunningResult.History> histories =
                runningFacade.getWeeklyHistories(new RunningCriteria.Weekly(requester.memberId(), date));

        return ApiResponse.success(CalendarResponse.Weekly.from(histories));
    }
}
