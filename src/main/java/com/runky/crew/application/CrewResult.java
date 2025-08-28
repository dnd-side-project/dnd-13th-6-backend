package com.runky.crew.application;

import java.math.BigDecimal;
import java.util.List;

import com.runky.crew.domain.Crew;

public record CrewResult(
	Long id,
	String name,
	String code,
	Long leaderId,
	String notice,
	Long memberCount
) {
	public static CrewResult from(Crew crew) {
		return new CrewResult(
			crew.getId(),
			crew.getName(),
			crew.getCode().value(),
			crew.getLeaderId(),
			crew.getNotice(),
			crew.getActiveMemberCount()
		);
	}

	public record Card(
		Long crewId,
		String crewName,
		Long memberCount,
		boolean isLeader,
		List<String> badgeImageUrls,
		BigDecimal goal,
		Double runningDistance,
		boolean isRunning
	) {
	}

	public record Detail(
		Long crewId,
		String name,
		String leaderNickname,
		String notice,
		Long memberCount,
		BigDecimal goal,
		Double runningDistance,
		String code
	) {
	}

	public record Running(
		Long memberId,
		boolean isRunning
	) {
	}

	public record CrewMember(
		Long memberId,
		String nickname,
		String badgeImageUrl,
		Double runningDistance,
		boolean isRunning,
		String sub
	) {
	}

	public record Leave(
		Long crewId,
		String name
	) {
	}

	public record Delegate(
		Long leaderId,
		String leaderNickname
	) {
	}

	public record Ban(
		Long targetId,
		String nickname
	) {
	}

	public record RelatedRunningMember(
		String nickname
	) {
	}
}
