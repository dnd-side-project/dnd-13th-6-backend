package com.runky.running.interfaces.api;

import static com.runky.running.interfaces.api.RunningResponse.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.runky.running.interfaces.websocket.LocationMessage;
import com.runky.running.interfaces.websocket.RoomEvent;
import com.runky.running.interfaces.websocket.WsDestinations;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/runnings")
@RequiredArgsConstructor
public class RunningController implements RunningApiSpec {
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final RunningFacade runningFacade;
	private final SimpMessagingTemplate messagingTemplate;

	@PostMapping("{runningId}/location/publish")
	public ApiResponse<ApiResponse<Void>> publish(
		@DestinationVariable Long runningId,
		@AuthenticationPrincipal MemberPrincipal requester,
		@Validated LocationMessage payload
	) {
		Long runnerId = requester.memberId();
		RoomEvent event = new RoomEvent("LOCATION", runningId, runnerId, payload.x(), payload.y(), payload.timestamp());
		String dest = "/topic/runnings/" + runningId;
		messagingTemplate.convertAndSend(dest, event);

		return ApiResponse.ok();
	}

	@Override
	@PostMapping("/start")
	public ApiResponse<Start> start(
		@AuthenticationPrincipal MemberPrincipal requester) {
		RunningResult.Start result = runningFacade.start(new RunningCriteria.Start(requester.memberId()));

		String publish = WsDestinations.publish(result.runningId());
		String subscribe = WsDestinations.subscribe(result.runningId());

		Start response = Start.from(
			publish, subscribe, result);
		return ApiResponse.success(response);
	}

	@Override
	@PostMapping("/{runningId}/end")
	public ApiResponse<End> end(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long runningId,
		@RequestBody com.runky.running.interfaces.api.RunningRequest.End request
	) {
		RunningCriteria.End criteria = request.toCriteria(runningId, requester.memberId());
		RunningResult.End result = runningFacade.end(criteria);

		End response = End.from(result);
		return ApiResponse.success(response);
	}

	@GetMapping("/today")
	public ApiResponse<TodaySummary> getToday(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		var result = runningFacade.getTodaySummary(new RunningCriteria.TodaySummary(requester.memberId(),
			LocalDateTime.now().atZone(KST).toLocalDateTime()));
		return ApiResponse.success(TodaySummary.from(result));
	}

	@GetMapping("/me/weekly/total-distance")
	public ApiResponse<MyWeeklyTotalDistance> getMyWeeklyTotalDistance(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		var result = runningFacade.getMyWeeklyTotalDistance(
			new RunningCriteria.MyWeeklyTotalDistance(requester.memberId()));
		return ApiResponse.success(MyWeeklyTotalDistance.from(result));
	}

	@GetMapping("/{runningId}")
	public ApiResponse<RunResult> getRunResult(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable("runningId") Long runningId
	) {
		var result = runningFacade.getRunResult(new RunningCriteria.RunResult(requester.memberId(), runningId));
		return ApiResponse.success(RunResult.from(result));
	}

	@DeleteMapping("/{runningId}/active")
	public ApiResponse<RemovedRunning> removeActiveRunning(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long runningId
	) {
		var result = runningFacade.removeActiveRunning(
			new RunningCriteria.RemoveActiveRunning(requester.memberId(), runningId));
		return ApiResponse.success(RemovedRunning.from(result));
	}
}
