package com.runky.notification.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.notification.application.DeviceTokenFacade;
import com.runky.notification.application.DeviceTokenResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController implements DeviceTokenApiSpec {

	private final DeviceTokenFacade deviceTokenFacade;

	@PostMapping
	public ApiResponse<Void> register(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestBody DeviceTokenRequest.Register request
	) {
		deviceTokenFacade.register(request.toCriteria(userId));
		return ApiResponse.ok();
	}

	@DeleteMapping
	public ApiResponse<DeviceTokenResponse.Delete> delete(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestBody DeviceTokenRequest.Delete request
	) {
		DeviceTokenResult.Delete result = deviceTokenFacade.delete(request.toCriteria(userId));
		return ApiResponse.success(new DeviceTokenResponse.Delete(result.count()));
	}
}
