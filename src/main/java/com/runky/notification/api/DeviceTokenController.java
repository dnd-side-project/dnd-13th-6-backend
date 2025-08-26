package com.runky.notification.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.notification.application.NotificationFacade;
import com.runky.notification.application.NotificationResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController implements DeviceTokenApiSpec {

	private final NotificationFacade notificationFacade;

	@PostMapping
	public ApiResponse<Void> register(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody DeviceTokenRequest.Register request
	) {
		notificationFacade.registerDeviceToken(request.toCriteria(requester.memberId()));
		return ApiResponse.ok();
	}

	@DeleteMapping
	public ApiResponse<DeviceTokenResponse.Delete> delete(
		@AuthenticationPrincipal MemberPrincipal requester,
		@RequestBody DeviceTokenRequest.Delete request
	) {
		NotificationResult.DeviceTokenDeletionResult result = notificationFacade.deleteDeviceToken(
			request.toCriteria(requester.memberId()));
		return ApiResponse.success(new DeviceTokenResponse.Delete(result.count()));
	}
}
