package com.runky.cheer.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.cheer.application.CheerCriteria;
import com.runky.cheer.application.CheerFacade;
import com.runky.cheer.application.CheerResult;
import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/runnings/{runningId}/cheers")
@RequiredArgsConstructor
public class CheerController implements CheerApiSpec {

	private final CheerFacade cheerFacade;

	@PostMapping
	public ApiResponse<CheerResponse.Sent> send(
		@AuthenticationPrincipal MemberPrincipal requester,
		@PathVariable Long runningId,
		@RequestBody CheerRequest.Send request
	) {
		CheerResult.Sent result = cheerFacade.send(
			new CheerCriteria.Send(runningId, requester.memberId(), request.receiverId(), request.message())
		);
		return ApiResponse.success(CheerResponse.Sent.from(result));
	}
	
}
