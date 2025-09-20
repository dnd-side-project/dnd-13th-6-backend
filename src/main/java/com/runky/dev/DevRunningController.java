package com.runky.dev;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runky.global.response.ApiResponse;
import com.runky.running.infra.jpa.RunningJpaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dev/api/running")
@RequiredArgsConstructor
public class DevRunningController {
	private final RunningJpaRepository repository;

	@DeleteMapping("/{runningId}/active")
	public ApiResponse<Void> removeActiveRunning(@PathVariable Long runningId) {
		repository.deleteById(runningId);
		return ApiResponse.ok();
	}
}
