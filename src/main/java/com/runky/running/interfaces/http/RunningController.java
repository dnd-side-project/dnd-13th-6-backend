package com.runky.running.interfaces.http;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.running.api.socket.WsDestinations;
import com.runky.running.application.RunningCriteria;
import com.runky.running.application.RunningFacade;
import com.runky.running.application.RunningResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/runnings")
@RequiredArgsConstructor
public class RunningController implements com.runky.running.interfaces.http.RunningApiSpec {
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final RunningFacade runningFacade;

	@Override
	@PostMapping("/start")
	public ApiResponse<com.runky.running.interfaces.http.RunningResponse.Start> start(
		@AuthenticationPrincipal MemberPrincipal requester) {
		RunningResult.Start result = runningFacade.start(new RunningCriteria.Start(requester.memberId()));

		String publish = WsDestinations.publish(result.runningId());
		String subscribe = WsDestinations.subscribe(result.runningId());

		RunningResponse.Start response = RunningResponse.Start.from(
			publish, subscribe, result);
		return ApiResponse.success(response);
	}

	@Override
	@PostMapping("/{runningId}/end")
	public ApiResponse<RunningResponse.End> end(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long runningId,
		@RequestBody com.runky.running.api.http.RunningRequest.End request
	) {
		RunningCriteria.End criteria = request.toCriteria(runningId, requester.memberId());
		RunningResult.End result = runningFacade.end(criteria);

		RunningResponse.End response = RunningResponse.End.from(
			result);
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

	@GetMapping("/{runningId}")
	public ApiResponse<com.runky.running.interfaces.http.RunningResponse.RunResult> getRunResult(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable("runningId") Long runningId
	) {
		var result = runningFacade.getRunResult(new RunningCriteria.RunResult(requester.memberId(), runningId));
		return ApiResponse.success(RunningResponse.RunResult.from(result));
	}

	@DeleteMapping("/{runningId}/active")
	public ApiResponse<RunningResponse.RemovedRunning> removeActiveRunning(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long runningId
	) {
		var result = runningFacade.removeActiveRunning(
			new RunningCriteria.RemoveActiveRunning(requester.memberId(), runningId));
		return ApiResponse.success(RunningResponse.RemovedRunning.from(result));
	}
}
