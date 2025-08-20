package com.runky.running.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningFacade {
	private final RunningService runningService;

	@Transactional
	public RunningResult.Start start(RunningCriteria.Start criteria) {
		RunningInfo.Start info = runningService.start(criteria.toCommand());
		return RunningResult.Start.from(info);
	}

	@Transactional
	public RunningResult.End end(RunningCriteria.End criteria) {
		RunningInfo.End info = runningService.end(criteria.toCommand());
		return RunningResult.End.from(info);
	}

}
