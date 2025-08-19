package com.runky.crew.api;

import com.runky.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Crew API", description = "Runky Crew API입니다.")
public interface CrewApiSpec {

    @Operation(
            summary = "크루 생성",
            description = "크루를 생성합니다."
    )
    ApiResponse<CrewResponse.Create> createCrew(
            @Schema(name = "크루 생성 요청", description = "크루 생성에 필요한 정보") CrewRequest.Create request, Long userId
    );

    @Operation(
            summary = "크루 가입",
            description = "크루에 가입합니다."
    )
    ApiResponse<CrewResponse.Join> joinCrew(
            @Schema(name = "크루 가입 요청", description = "크루 가입에 필요한 정보") CrewRequest.Join request, Long userId
    );

    @Operation(
            summary = "크루 목록 조회",
            description = "사용자가 속한 크루 목록을 조회합니다."
    )
    ApiResponse<CrewResponse.Cards> getCrews(
            Long userId
    );

    @Operation(
            summary = "크루 상세 조회",
            description = "크루의 상세 정보를 조회합니다."
    )
    ApiResponse<CrewResponse.Detail> getCrew(
            @Schema(name = "크루 ID", description = "상세 조회할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루 탈퇴",
            description = "크루에서 탈퇴합니다."
    )
    ApiResponse<CrewResponse.Leave> leaveCrew(
            @Schema(name = "멤버 ID", description = "새 리더로 지정할 멤버 ID") CrewRequest.Leave request,
            @Schema(name = "크루 ID", description = "탈퇴할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루원 목록 조회",
            description = "크루의 멤버를 조회합니다."
    )
    ApiResponse<CrewResponse.Members> getCrewMembers(
            @Schema(name = "크루 ID", description = "멤버를 조회할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루 공지사항 수정",
            description = "크루의 공지사항을 수정합니다."
    )
    ApiResponse<CrewResponse.Notice> updateNotice(
            @Schema(name = "크루 공지사항 수정 요청", description = "크루 공지사항 수정에 필요한 정보") CrewRequest.Notice request,
            @Schema(name = "크루 ID", description = "공지사항을 수정할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루 이름 수정",
            description = "크루의 이름을 수정합니다."
    )
    ApiResponse<CrewResponse.Name> updateName(
            @Schema(name = "크루 이름 수정 요청", description = "크루 이름 수정에 필요한 정보") CrewRequest.Name request,
            @Schema(name = "크루 ID", description = "이름을 수정할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루 해체",
            description = "크루를 해체합니다."
    )
    ApiResponse<CrewResponse.Disband> disbandCrew(
            @Schema(name = "크루 ID", description = "해체할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루 리더 위임",
            description = "크루의 리더를 다른 멤버로 위임합니다."
    )
    ApiResponse<CrewResponse.Delegate> delegateLeader(
            @Schema(name = "크루 리더 위임 요청", description = "새 리더의 ID") CrewRequest.Delegate request,
            @Schema(name = "크루 ID", description = "리더를 위임할 크루 ID") Long crewId,
            Long userId
    );

    @Operation(
            summary = "크루원 추방",
            description = "크루에서 특정 멤버를 추방합니다."
    )
    ApiResponse<CrewResponse.Ban> banMember(
            @Schema(name = "크루 ID", description = "멤버를 추방할 크루 ID") Long crewId,
            @Schema(name = "추방할 사용자 ID", description = "추방할 멤버의 ID") Long targetId,
            Long userId
    );
}
