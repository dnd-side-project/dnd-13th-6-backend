package com.runky.dev;

import org.springframework.web.bind.annotation.PathVariable;

import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dev Running API", description = "관리자 Running API 입니다.")
public interface DevRunningApiSpec {

	@Operation(
		summary = "활성 러닝 강제 제거 (DEV)",
		description = "지정한 러닝 ID의 레코드를 관리자 권한으로 강제 삭제합니다. (주의: 복구 불가)"
	)
	ApiResponse<Void> removeActiveRunning(
		@Parameter(description = "강제 제거할 러닝 ID", example = "123") @PathVariable Long runningId
	);
}
