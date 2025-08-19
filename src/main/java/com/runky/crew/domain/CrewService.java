package com.runky.crew.domain;

import com.runky.crew.error.CrewErrorCode;
import com.runky.global.error.GlobalErrorCode;
import com.runky.global.error.GlobalException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrewService {
    private final CrewRepository crewRepository;
    private final CodeGenerator codeGenerator;

    @Transactional
    public Crew create(CrewCommand.Create command) {
        CrewMemberCount crewMemberCount = crewRepository.findCountByMemberId(command.userId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
        crewMemberCount.increment();

        Crew crew = Crew.of(command, codeGenerator.generate());
        return crewRepository.save(crew);
    }

    @Transactional
    public Crew join(CrewCommand.Join command) {
        Crew crew = crewRepository.findCrewByCode(new Code(command.code()))
                .orElseThrow(() -> new GlobalException(CrewErrorCode.NOT_FOUND_CREW));

        crew.joinMember(command.userId());

        CrewMemberCount crewMemberCount = crewRepository.findCountByMemberId(command.userId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
        crewMemberCount.increment();

        return crew;
    }

    @Transactional(readOnly = true)
    public List<Crew> getCrewsOfUser(Long userId) {
        return crewRepository.findCrewsByMemberId(userId);
    }

    @Transactional(readOnly = true)
    public Crew getCrew(CrewCommand.Detail command) {
        Crew crew = crewRepository.findById(command.crewId())
                .orElseThrow(() -> new GlobalException(CrewErrorCode.NOT_FOUND_CREW));
        if (crew.doesNotContainMember(command.userId())) {
            throw new GlobalException(CrewErrorCode.NOT_CREW_MEMBER);
        }
        return crew;
    }

    @Transactional(readOnly = true)
    public List<CrewMember> getCrewMembers(CrewCommand.Members command) {
        Crew crew = crewRepository.findById(command.crewId())
                .orElseThrow(() -> new GlobalException(CrewErrorCode.NOT_FOUND_CREW));
        if (crew.doesNotContainMember(command.userId())) {
            throw new GlobalException(CrewErrorCode.NOT_CREW_MEMBER);
        }
        return crew.getActiveMembers();
    }

    @Transactional
    public Crew leave(CrewCommand.Leave command) {
        Crew crew = crewRepository.findById(command.crewId())
                .orElseThrow(() -> new GlobalException(CrewErrorCode.NOT_FOUND_CREW));
        if (crew.doesNotContainMember(command.userId())) {
            throw new GlobalException(CrewErrorCode.NOT_CREW_MEMBER);
        }
        CrewMember member = crew.getMember(command.userId());
        if (member.isLeader()) {
            crew.delegateLeader(command.newLeaderId());
        }
        crew.leaveMember(command.userId());
        CrewMemberCount crewMemberCount = crewRepository.findCountByMemberId(command.userId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
        crewMemberCount.decrement();
        return crew;
    }
}
