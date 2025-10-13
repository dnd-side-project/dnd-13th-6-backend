package com.runky.crew.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewMember;
import com.runky.crew.domain.CrewMemberCount;
import com.runky.crew.domain.CrewRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CrewRepositoryImpl implements CrewRepository {

	private final CrewJpaRepository crewJpaRepository;
	private final CrewMemberJpaRepository crewMemberJpaRepository;
	private final CrewMemberCountJpaRepository crewMemberCountJpaRepository;

	@Override
	public Crew save(Crew crew) {
		return crewJpaRepository.save(crew);
	}

	@Override
	public Optional<Crew> findById(Long crewId) {
		return crewJpaRepository.findCrewJoinFetch(crewId);
	}

	@Override
	public Optional<Crew> findCrewByCode(Code code) {
		return crewJpaRepository.findByCode(code);
	}

	@Override
	public List<Crew> findCrewsByMemberId(Long memberId) {
		return crewJpaRepository.findCrewsByMemberId(memberId);
	}

	@Override
	public Optional<CrewMember> findByCrewAndMember(Long crewId, Long memberId) {
		return crewMemberJpaRepository.findByCrewIdAndMemberId(crewId, memberId);
	}

	@Override
	public List<CrewMember> findCrewMemberOfUser(Long memberId) {
		return crewMemberJpaRepository.findByMemberId(memberId);
	}

	@Override
	public CrewMemberCount save(CrewMemberCount crewMemberCount) {
		return crewMemberCountJpaRepository.save(crewMemberCount);
	}

    @Override
    public CrewMember save(CrewMember crewMember) {
        return crewMemberJpaRepository.save(crewMember);
    }

    @Override
	public List<CrewMemberCount> findCrewMemberCounts(Set<Long> userIds) {
		return crewMemberCountJpaRepository.findByMemberIdIn(userIds);
	}

	@Override
	public List<Long> findAllCrewMembersOfUserWithoutUserId(Long userId) {
		return crewJpaRepository.findAllCrewMembersOfUserWithoutUserId(userId);
	}

	@Override
	public Optional<CrewMemberCount> findCountByMemberId(Long memberId) {
		return crewMemberCountJpaRepository.findByMemberId(memberId);
	}

    @Override
    public List<CrewMember> findRelatedCrewMembers(Long userId) {
        return crewMemberJpaRepository.findRelatedCrewMembers(userId);
    }

	@Override
	public void deleteCrewMemberCountByMemberId(Long memberId) {
		crewMemberCountJpaRepository.deleteByMemberId(memberId);
	}

	@Override
	public void deleteCrewMembersByMemberId(Long memberId) {
		crewMemberJpaRepository.deleteByMemberId(memberId);
	}

	@Override
	public void deleteCrew(Crew crew) {
		crewJpaRepository.delete(crew);
		crewMemberJpaRepository.deleteByCrew(crew);
	}
}
