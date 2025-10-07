package com.runky.running.interfaces.api;

import static com.runky.running.interfaces.api.RunningResponse.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
import com.runky.goal.application.GoalCriteria;
import com.runky.goal.application.GoalFacade;
import com.runky.goal.application.MemberGoalSnapshotResult;
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
	private final GoalFacade goalFacade;
	private final SimpMessagingTemplate messagingTemplate;

	@PostMapping("/{runningId}/location/publish")
	public ApiResponse<Void> publish(
		@PathVariable Long runningId,
		@AuthenticationPrincipal MemberPrincipal requester,
		@Validated @RequestBody LocationMessage payload
	) {
		Long runnerId = requester.memberId();
		RoomEvent event = new RoomEvent("LOCATION", runningId, runnerId, payload.x(), payload.y(), payload.timestamp());
		String dest = "/topic/runnings/" + runningId;
		messagingTemplate.convertAndSend(dest, event);

		return ApiResponse.ok();
	}

	@Override
	@PostMapping("/start")
	public ApiResponse<RunningResponse.Start> start(
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
		@RequestBody RunningRequest.End request
	) {
		RunningCriteria.End criteria = request.toCriteria(runningId, requester.memberId());
		RunningResult.End result = runningFacade.end(criteria);
		End response = End.from(result);

		messagingTemplate.convertAndSend("/topic/runnings/" + runningId,
			new RoomEvent("ENDED", runningId, requester.memberId(), null, null, System.currentTimeMillis()));

		return ApiResponse.success(response);
	}

	@Override
	@PostMapping("/end")
	public ApiResponse<End> end(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody RunningRequest.End request
	) {
		RunningCriteria.EndWithNoRunningId criteria = request.toCriteria(requester.memberId());
		RunningResult.End result = runningFacade.end(criteria);
		Long runningId = result.runningId();
		messagingTemplate.convertAndSend("/topic/runnings/" + runningId,
			new RoomEvent("ENDED", runningId, requester.memberId(), null, null, System.currentTimeMillis()));

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

	// TODO 추후 Goal API로 이동
	@GetMapping("/me/weekly/total-distance")
	public ApiResponse<MyWeeklyTotalDistance> getMyWeeklyTotalDistance(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		MemberGoalSnapshotResult snapshot =
			goalFacade.getMemberGoalSnapshot(new GoalCriteria.MemberGoal(requester.memberId()));

		double km = snapshot.distance().doubleValue();
		double meter = km * 1000;

		return ApiResponse.success(new RunningResponse.MyWeeklyTotalDistance(km, meter));
	}

	@GetMapping("/{runningId}")
	public ApiResponse<MyWeeklyTotalDistance> getRunResult(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		MemberGoalSnapshotResult snapshot =
			goalFacade.getMemberGoalSnapshot(new GoalCriteria.MemberGoal(requester.memberId()));

		double km = snapshot.distance().doubleValue();
		double meter = km * 1000;

		return ApiResponse.success(new RunningResponse.MyWeeklyTotalDistance(km, meter));
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
