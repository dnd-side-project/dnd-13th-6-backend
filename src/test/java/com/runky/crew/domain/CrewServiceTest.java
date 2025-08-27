package com.runky.crew.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.runky.crew.error.CrewErrorCode;
import com.runky.global.error.GlobalErrorCode;
import com.runky.global.error.GlobalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrewServiceTest {

    @InjectMocks
    private CrewService crewService;
    @Mock
    private CrewRepository crewRepository;
    @Mock
    private CodeGenerator codeGenerator;

    @DisplayName("크루 생성 시,")
    @Nested
    class Create {

        @Test
        @DisplayName("사용자의 크루 가입 수 정보가 없는 경우, NOT_FOUND 예외를 발생시킨다.")
        void throwNotFoundException_whenMemberCrewCountNotFound() {
            CrewCommand.Create command = new CrewCommand.Create(1L, "Test Crew");
            given(crewRepository.findCountByMemberId(command.userId()))
                    .willReturn(Optional.empty());

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.create(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(GlobalErrorCode.NOT_FOUND));
        }
    }

    @DisplayName("크루 가입 시,")
    @Nested
    class Join {

        @Test
        @DisplayName("탈퇴했던 크루일 경우, 재가입 처리된다.")
        void rejoinCrew_whenAlreadyJoined() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "Crew"), new Code("ABC123"));
            crew.joinMember(2L);
            crew.leaveMember(2L);
            given(crewRepository.findCrewByCode(crew.getCode()))
                    .willReturn(Optional.of(crew));
            given(crewRepository.findCountByMemberId(2L))
                    .willReturn(Optional.of(CrewMemberCount.of(2L)));
            CrewCommand.Join command = new CrewCommand.Join(2L, crew.getCode().value());

            Crew joinedCrew = crewService.join(command);

            assertThat(joinedCrew.getMember(2L).getRole()).isEqualTo(CrewMember.Role.MEMBER);
            assertThat(joinedCrew.getMember(2L).isInCrew()).isTrue();
        }

        @Test
        @DisplayName("크루에 처음 가입하는 경우, 새로운 멤버로 추가된다.")
        void joinCrew_whenFirstTimeJoining() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "New Crew"), new Code("XYZ789"));
            given(crewRepository.findCrewByCode(crew.getCode()))
                    .willReturn(Optional.of(crew));
            given(crewRepository.findCountByMemberId(3L))
                    .willReturn(Optional.of(CrewMemberCount.of(3L)));
            CrewCommand.Join command = new CrewCommand.Join(3L, crew.getCode().value());

            Crew joinedCrew = crewService.join(command);

            assertThat(joinedCrew.getMember(3L).getRole()).isEqualTo(CrewMember.Role.MEMBER);
            assertThat(joinedCrew.getMember(3L).isInCrew()).isTrue();
        }
    }

    @Nested
    @DisplayName("크루 상세 조회 시,")
    class View {
        @Test
        @DisplayName("크루가 존재하지 않으면 NOT_FOUND_CREW 예외를 발생시킨다.")
        void throwNotFoundCrewException_whenCrewNotFound() {
            CrewCommand.Detail command = new CrewCommand.Detail(1L, 2L);
            given(crewRepository.findById(command.crewId()))
                    .willReturn(Optional.empty());

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.getCrew(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.NOT_FOUND_CREW));
        }

        @Test
        @DisplayName("사용자가 크루 멤버가 아니면 NOT_CREW_MEMBER 예외를 발생시킨다.")
        void throwNotCrewMemberException_whenUserNotInCrew() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "Test Crew"), new Code("abc123"));
            given(crewRepository.findById(crew.getId()))
                    .willReturn(Optional.of(crew));
            CrewCommand.Detail command = new CrewCommand.Detail(crew.getId(), 3L);

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.getCrew(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.NOT_CREW_MEMBER));
        }
    }

    @Nested
    @DisplayName("크루원 목록 조회 시,")
    class FindMembers {

        @Test
        @DisplayName("크루가 존재하지 않으면 NOT_FOUND_CREW 예외를 발생시킨다.")
        void throwNotFoundCrewException_whenCrewNotFound() {
            CrewCommand.Members command = new CrewCommand.Members(1L, 2L);
            given(crewRepository.findById(command.crewId()))
                    .willReturn(Optional.empty());

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.getActiveCrewMembers(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.NOT_FOUND_CREW));
        }

        @Test
        @DisplayName("사용자가 크루 멤버가 아니면 NOT_CREW_MEMBER 예외를 발생시킨다.")
        void throwNotCrewMemberException_whenUserNotInCrew() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "Test Crew"), new Code("xyz789"));
            given(crewRepository.findById(crew.getId()))
                    .willReturn(Optional.of(crew));
            CrewCommand.Members command = new CrewCommand.Members(crew.getId(), 3L);

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.getActiveCrewMembers(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.NOT_CREW_MEMBER));
        }

        @Test
        @DisplayName("현재 크루에 활동중인 멤버만 조회된다.")
        void getActiveMembers() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "Active Crew"), new Code("abc123"));
            crew.joinMember(2L);
            crew.joinMember(3L);
            crew.leaveMember(3L);
            given(crewRepository.findById(crew.getId()))
                    .willReturn(Optional.of(crew));
            CrewCommand.Members command = new CrewCommand.Members(crew.getId(), 1L);

            List<CrewMember> members = crewService.getActiveCrewMembers(command);

            assertThat(members).hasSize(2);
            assertThat(members)
                    .extracting("memberId")
                    .containsExactlyInAnyOrder(1L, 2L);
        }
    }

    @Nested
    @DisplayName("크루원 탈퇴 시,")
    class Leave {

        @Test
        @DisplayName("크루가 존재하지 않으면 NOT_FOUND_CREW 예외를 발생시킨다.")
        void throwNotFoundCrewException_whenCrewNotFound() {
            CrewCommand.Leave command = new CrewCommand.Leave(1L, 2L, null);
            given(crewRepository.findById(command.crewId()))
                    .willReturn(Optional.empty());

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.leave(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.NOT_FOUND_CREW));
        }

        @Test
        @DisplayName("리더가 탈퇴할 경우 새로운 리더를 지정하지 않으면, HAVE_TO_DELEGATE_LEADER 예외를 발생시킨다.")
        void throwHaveToDelegateLeaderException_whenLeaderLeavesWithoutDelegation() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "Test Crew"), new Code("def456"));
            crew.joinMember(2L);
            given(crewRepository.findById(crew.getId()))
                    .willReturn(Optional.of(crew));
            CrewCommand.Leave command = new CrewCommand.Leave(crew.getId(), 1L, null);

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.leave(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.HAVE_TO_DELEGATE_LEADER));
        }

        @Test
        @DisplayName("사용자가 크루 멤버가 아니면 NOT_CREW_MEMBER 예외를 발생시킨다.")
        void throwNotCrewMemberException_whenUserNotInCrew() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "Test Crew"), new Code("xyz789"));
            crew.joinMember(3L);
            crew.leaveMember(3L);
            given(crewRepository.findById(crew.getId()))
                    .willReturn(Optional.of(crew));
            CrewCommand.Leave command = new CrewCommand.Leave(crew.getId(), 3L, null);

            GlobalException thrown = assertThrows(GlobalException.class, () -> crewService.leave(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(CrewErrorCode.NOT_CREW_MEMBER));
        }
    }

    @Nested
    @DisplayName("모든 크루의 활동 멤버 정보 조회 시,")
    class GetActiveMemberInfos {

        @Test
        @DisplayName("크루 관련 멤버 정보 중, 활동중인 멤버 정보만 조회된다.")
        void getActiveMemberInfos() {
            Crew crew1 = Crew.of(new CrewCommand.Create(1L, "Crew One"), new Code("code12"));
            crew1.joinMember(2L);
            crew1.joinMember(3L);
            crew1.leaveMember(3L);

            Crew crew2 = Crew.of(new CrewCommand.Create(4L, "Crew Two"), new Code("code22"));
            crew2.joinMember(5L);
            crew2.joinMember(6L);
            crew2.banMember(6L);

            given(crewRepository.findCrewsByMemberId(1L))
                    .willReturn(List.of(crew1, crew2));

            List<Crew> crews = crewService.getCrewsOfUser(1L);

            assertThat(crews).hasSize(2);
            assertThat(crews.get(0).getActiveMembers())
                    .extracting("memberId")
                    .containsExactlyInAnyOrder(1L, 2L);
            assertThat(crews.get(1).getActiveMembers())
                    .extracting("memberId")
                    .containsExactlyInAnyOrder(4L, 5L);
        }
    }
}
