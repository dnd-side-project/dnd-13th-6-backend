package com.runky.notification.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.notification.application.NotificationCriteria;
import com.runky.notification.application.NotificationFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/api/device-tokens")
@RequiredArgsConstructor
public class TestPushController {
	private final NotificationFacade notificationFacade;

	@PostMapping("/sendToOne")
	public ApiResponse<?> sendToOne(@RequestBody SendToOne request) {
		var result = notificationFacade.pushToOne(
			new NotificationCriteria.PushToOne(request.memberId, request.title, request.body, request.data));
		return ApiResponse.success(result);
	}

	@PostMapping("/sendToMany")
	public ApiResponse<?> sendToMany(@RequestBody SendToMany request) {
		var result = notificationFacade.pushToMany(
			new NotificationCriteria.PushToMany(request.memberIds, request.title, request.body, request.data));
		return ApiResponse.success(result);
	}

	record SendToOne(Long memberId, String title, String body, Map<String, String> data) {
	}

	record SendToMany(List<Long> memberIds, String title, String body, Map<String, String> data) {
	}
}
