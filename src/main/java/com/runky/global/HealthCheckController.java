package com.runky.global;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.security.auth.MemberPrincipal;

@RestController("/health")
public class HealthCheckController {

	@GetMapping("/health")
	public String healthCheck() {
		return "hi";
	}

	@GetMapping("/me/authentication")
	public String healthCheck(@AuthenticationPrincipal MemberPrincipal me) {
		return "유저 Id" + me.memberId();
	}

}
