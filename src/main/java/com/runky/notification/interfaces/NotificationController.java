package com.runky.notification.interfaces;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.notification.application.NotificationFacade;
import com.runky.notification.application.NotificationResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApiSpec {
	private static final int DEFAULT_LIMIT = 10;

	private final NotificationFacade notificationFacade;

	/** 최신순 10개 고정 */
	@GetMapping("/recent")
	public ApiResponse<NotificationResponse.Items> getRecentTop10(
		@AuthenticationPrincipal MemberPrincipal requester
	) {
		NotificationRequest.GetRecentTopN request = new NotificationRequest.GetRecentTopN(requester.memberId(),
			DEFAULT_LIMIT);
		NotificationResult.Items result = notificationFacade.recentTopN(request.toCriteria());
		return ApiResponse.success(NotificationResponse.Items.from(result));
	}

}
