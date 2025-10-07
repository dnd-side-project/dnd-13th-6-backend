package com.runky.running.interfaces.api;

import com.runky.running.application.RunningCriteria;

public final class RunningRequest {
	private RunningRequest() {
	}

	public record End(Summary summary, Track track) {
		public RunningCriteria.End toCriteria(Long runningId, Long runnerId) {
			return new RunningCriteria.End(
				runningId, runnerId,
				summary.totalDistanceMeter, summary.durationSeconds, summary.avgSpeedMPS,
				track.format, track.points, track.pointCount
			);
		}

		public RunningCriteria.EndWithNoRunningId toCriteria(Long runnerId) {
			return new RunningCriteria.EndWithNoRunningId(
				runnerId,
				summary.totalDistanceMeter, summary.durationSeconds, summary.avgSpeedMPS,
				track.format, track.points, track.pointCount
			);

		}
	}

	public record Summary(Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMPS) {
	}

	public record Track(String format, String points, int pointCount) {
	}

}
