package com.runky.reward.api;

import com.runky.global.response.ApiResponse;
import com.runky.reward.api.RewardResponse.Images;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reward API", description = "Runky Reward API입니다.")
public interface RewardApiSpec {

    @Operation(
            summary = "내 캐릭터 목록 조회",
            description = "내가 보유한 캐릭터 목록을 조회합니다."
    )
    ApiResponse<Images> getBadges(
            @Schema(name = "사용자 ID", description = "X-USER-ID로 로그인 대체") Long userId
    );

    @Operation(
            summary = "캐릭터 뽑기",
            description = "클로버를 사용해 캐릭터를 뽑습니다."
    )
    ApiResponse<RewardResponse.Draw> drawCharacter(
            @Schema(name = "사용자 ID", description = "X-USER-ID로 로그인 대체") Long userId
    );

    @Operation(
            summary = "클로버 개수 조회",
            description = "사용자의 클로버 개수를 조회합니다."
    )
    ApiResponse<RewardResponse.Clover> getCloverCount(
            @Schema(name = "사용자 ID", description = "X-USER-ID로 로그인 대체") Long userId
    );
}
