package com.runky.crew.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.runky.crew.error.CrewErrorCode;
import com.runky.global.entity.BaseTimeEntity;
import com.runky.global.error.GlobalErrorCode;
import com.runky.global.error.GlobalException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "crew")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "leader_id", nullable = false)
	private Long leaderId;

	@OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<CrewMember> members = new ArrayList<>();

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "code", nullable = false, unique = true)
	private Code code;

	@Column(name = "notice")
	private String notice;

	@Column(name = "member_count", nullable = false)
	private Long activeMemberCount;

	@Version
	private Long version;

	private Crew(Long leaderId, String name, Code code, String notice, Long activeMemberCount) {
		this.leaderId = leaderId;
		this.name = name;
		this.code = code;
		this.notice = notice;
		this.activeMemberCount = activeMemberCount;
	}

	public static Crew of(CrewCommand.Create command, Code code) {
		if (command.name().isBlank()) {
			throw new GlobalException(CrewErrorCode.BLANK_CREW_NAME);
		}
		if (command.name().length() > CrewConstants.MAX_CREW_NAME_LENGTH.value()) {
			throw new GlobalException(CrewErrorCode.OVER_CREW_NAME);
		}

		Crew crew = new Crew(command.userId(), command.name(), code, "", 0L);
		crew.add(CrewMember.leaderOf(crew));
		return crew;
	}

	public CrewMember joinMember(Long memberId) {
		if (hasHistory(memberId)) {
			CrewMember member = getMemberHistory(memberId);
			member.rejoin();
			incrementActiveMemberCount();
			return member;
		}
		CrewMember crewMember = CrewMember.memberOf(memberId, this);
		add(crewMember);
		return crewMember;
	}

	public CrewMember banMember(Long memberId) {
		CrewMember crewMember = getMember(memberId);
		crewMember.ban();
		decrementActiveMemberCount();
		return crewMember;
	}

	public CrewMember leaveMember(Long memberId) {
		CrewMember crewMember = getMember(memberId);
		crewMember.leave();
		decrementActiveMemberCount();
		return crewMember;
	}

	public void add(CrewMember crewMember) {
		crewMember.join(this);
		this.members.add(crewMember);
		incrementActiveMemberCount();
	}

	public void delegateLeader(Long newLeaderId) {
		if (newLeaderId == null) {
			throw new GlobalException(CrewErrorCode.HAVE_TO_DELEGATE_LEADER);
		}
		if (isAlone()) {
			throw new GlobalException(CrewErrorCode.NOT_ENOUGH_CREW_MEMBER);
		}

		CrewMember leader = getLeader();
		leader.demote();

		CrewMember member = getMember(newLeaderId);
		member.promote();
		this.leaderId = newLeaderId;
	}

	public List<CrewMember> disband() {
		CrewMember leader = getLeader();
		leader.demote();
		List<CrewMember> activeMembers = getActiveMembers();
		for (CrewMember member : activeMembers) {
			member.leave();
		}
		this.activeMemberCount = 0L;
		delete();
		return activeMembers;
	}

	public void updateNotice(String notice) {
		if (notice == null || notice.length() > CrewConstants.MAX_CREW_NOTICE_LENGTH.value()) {
			throw new GlobalException(CrewErrorCode.INVALID_NOTICE);
		}
		this.notice = notice;
	}

	public void updateName(String name) {
		if (name == null || name.isBlank()) {
			throw new GlobalException(CrewErrorCode.BLANK_CREW_NAME);
		}
		if (name.length() > CrewConstants.MAX_CREW_NAME_LENGTH.value()) {
			throw new GlobalException(CrewErrorCode.OVER_CREW_NAME);
		}
		this.name = name;
	}

	public boolean hasHistory(Long memberId) {
		return this.members.stream()
			.anyMatch(member -> member.getMemberId().equals(memberId));
	}

	public boolean doesNotContainMember(Long memberId) {
		return !containsMember(memberId);
	}

	public boolean containsMember(Long memberId) {
		return this.members.stream()
			.anyMatch(member -> member.getMemberId().equals(memberId) && member.isInCrew());
	}

	public boolean isLeader(Long memberId) {
		return this.leaderId.equals(memberId);
	}

	public boolean isAlone() {
		List<CrewMember> activeMembers = getActiveMembers();
		return activeMembers.size() == 1;
	}

	public void incrementActiveMemberCount() {
		if (this.activeMemberCount >= CrewConstants.CREW_CAPACITY.value()) {
			throw new GlobalException(CrewErrorCode.OVER_CREW_MEMBER_COUNT);
		}
		this.activeMemberCount++;
	}

	public void decrementActiveMemberCount() {
		if (this.activeMemberCount == 1) {
			throw new GlobalException(CrewErrorCode.LAST_CREW_MEMBER);
		}
		if (this.activeMemberCount <= 0) {
			throw new GlobalException(GlobalErrorCode.VALID_EXCEPTION);
		}
		this.activeMemberCount--;
	}

	public CrewMember getLeader() {
		return this.members.stream()
			.filter(member -> member.getRole() == CrewMember.Role.LEADER)
			.findFirst()
			.orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
	}

	public CrewMember getMemberHistory(Long memberId) {
		return this.members.stream()
			.filter(member -> member.getMemberId().equals(memberId))
			.findFirst()
			.orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
	}

	public CrewMember getMember(Long memberId) {
		return this.members.stream()
			.filter(member -> member.getMemberId().equals(memberId) && member.isInCrew())
			.findFirst()
			.orElseThrow(() -> new GlobalException(CrewErrorCode.NOT_CREW_MEMBER));
	}

	public List<CrewMember> getActiveMembers() {
		return this.members.stream()
			.filter(CrewMember::isInCrew)
			.toList();
	}

	public void delegateRandomLeader() {
		List<CrewMember> members = getActiveMembers().stream()
			.filter(member -> !member.isLeader())
			.toList();
		if (members.isEmpty()) {
			throw new GlobalException(CrewErrorCode.NOT_ENOUGH_CREW_MEMBER);
		}

		int leaderIndex = new Random().nextInt(members.size());

		delegateLeader(members.get(leaderIndex).getMemberId());
	}
}
