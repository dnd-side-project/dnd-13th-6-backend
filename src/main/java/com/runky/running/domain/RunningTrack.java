package com.runky.running.domain;

import com.runky.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "running_tracks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunningTrack extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "running_id", nullable = false)
	private Running running;

	@Column(name = "format", nullable = false)
	private String format;

	@Column(columnDefinition = "TEXT", name = "points", nullable = false)
	private String points;

	@Column(name = "point_count", nullable = false)
	private int pointCount;

	@Builder
	public RunningTrack(final Running running, final String points, final String format, final int pointCount) {
		this.running = running;
		this.points = points;
		this.format = format;
		this.pointCount = pointCount;
	}
}
