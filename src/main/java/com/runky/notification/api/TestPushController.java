package com.runky.notification.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.notification.domain.push.PushCommand;
import com.runky.notification.domain.push.PushService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/api/device-tokens")
@RequiredArgsConstructor
public class TestPushController {
	private final PushService pushService;

	@PostMapping("/sendToOne")
	public ApiResponse<?> sendToOne(@RequestBody SendToOne request) {
		var result = pushService.pushToOne(
			new PushCommand.Push.ToOne(request.memberId, request.title, request.body, request.data));
		return ApiResponse.success(result);
	}

	@PostMapping("/sendToMany")
	public ApiResponse<?> sendToMany(@RequestBody SendToMany request) {
		var result = pushService.pushToMany(
			new PushCommand.Push.ToMany(request.memberIds, request.title, request.body, request.data));
		return ApiResponse.success(result);
	}

	record SendToOne(Long memberId, String title, String body, Map<String, String> data) {
	}

	record SendToMany(List<Long> memberIds, String title, String body, Map<String, String> data) {
	}
}
