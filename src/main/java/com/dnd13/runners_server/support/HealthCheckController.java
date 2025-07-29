package com.dnd13.runners_server.support;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@GetMapping("/health")
	public String healthCheck(){
		return "hi";
	}

}
