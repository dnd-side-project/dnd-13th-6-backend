package com.runky.crew.domain;

public final class CrewCommand {
	private CrewCommand() {
	}

	public record Create(
		Long userId,
		String name
	) {
	}

	public record Join(
		Long userId,
		String code
	) {
	}

	public record Detail(
		Long crewId,
		Long userId
	) {
	}

	public record Leave(
		Long crewId,
		Long userId,
		Long newLeaderId
	) {
	}

	public record Members(
		Long crewId,
		Long userId
	) {
	}

	public record UpdateNotice(
		Long crewId,
		Long userId,
		String notice
	) {
	}

	public record UpdateName(
		Long crewId,
		Long userId,
		String name
	) {
	}

	public record Disband(
		Long crewId,
		Long userId
	) {
	}

	public record Delegate(
		Long crewId,
		Long userId,
		Long newLeaderId
	) {
	}

	public record Ban(
		Long crewId,
		Long userId,
		Long banMemberId
	) {
	}

	public record Related(
		Long userId
	) {
	}

	public record Init(
		Long memberId
	) {
	}
}
