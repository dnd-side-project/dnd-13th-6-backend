package com.runky.running.api;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.running.application.RunningCriteria;
import com.runky.running.application.RunningFacade;
import com.runky.running.application.RunningResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/runnings")
@RequiredArgsConstructor
public class RunningController implements RunningApiSpec {
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final RunningFacade runningFacade;

	@Override
	@PostMapping("/start")
	public ApiResponse<RunningResponse.Start> start(@AuthenticationPrincipal MemberPrincipal requester) {
		RunningResult.Start result = runningFacade.start(new RunningCriteria.Start(requester.memberId()));

		String publish = "/app/runnings/" + result.runningId() + "/location";

		RunningResponse.Start response = RunningResponse.Start.from(publish, result);
		return ApiResponse.success(response);
	}

	@Override
	@PostMapping("/{runningId}/end")
	public ApiResponse<RunningResponse.End> end(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long runningId,
		@RequestBody RunningRequest.End request
	) {
		RunningCriteria.End criteria = request.toCriteria(runningId, requester.memberId());
		RunningResult.End result = runningFacade.end(criteria);

		RunningResponse.End response = RunningResponse.End.from(result);
		return ApiResponse.success(response);
	}

	@GetMapping("/today")
	public ApiResponse<RunningResponse.TodaySummary> getToday(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		var result = runningFacade.getTodaySummary(new RunningCriteria.TodaySummary(requester.memberId(),
			LocalDateTime.now().atZone(KST).toLocalDateTime()));
		return ApiResponse.success(RunningResponse.TodaySummary.from(result));
	}

	@GetMapping("/me/weekly/total-distance")
	public ApiResponse<RunningResponse.MyWeeklyTotalDistance> getMyWeeklyTotalDistance(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		var result = runningFacade.getMyWeeklyTotalDistance(
			new RunningCriteria.MyWeeklyTotalDistance(requester.memberId()));
		return ApiResponse.success(RunningResponse.MyWeeklyTotalDistance.from(result));
	}
}
