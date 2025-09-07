package com.runky.running.api;

import com.runky.running.application.RunningCriteria;

public sealed interface RunningRequest {

	record End(
		Summary summary,
		Track track
	) implements RunningRequest {
		public RunningCriteria.End toCriteria(Long runningId, Long runnerId) {
			return new RunningCriteria.End(
				runningId,
				runnerId,
				summary.totalDistanceMinutes,
				summary.durationSeconds,
				summary.avgSpeedMPS,
				track.format,
				track.points,
				track.pointCount
			);
		}

		record Summary(
			Double totalDistanceMinutes,
			Long durationSeconds,
			Double avgSpeedMPS
		) {
		}

		record Track(
			String format,
			String points,
			int pointCount
		) {
		}

		record Point(
			Double lat,
			Double lng,
			Long timestamp
		) {
		}
	}
}
