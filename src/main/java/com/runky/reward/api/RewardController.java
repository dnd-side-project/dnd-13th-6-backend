package com.runky.reward.api;

import com.runky.global.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RewardController implements RewardApiSpec {
    @Override
    public ApiResponse<RewardResponse.Characters> getCharacters(Long userId) {
        return ApiResponse.success(new RewardResponse.Characters(
                List.of("/character1.png", "/character2.png", "/character3.png")));
    }

    @Override
    public ApiResponse<RewardResponse.Draw> drawCharacter(Long userId) {
        return ApiResponse.success(new RewardResponse.Draw("/character15.png"));
    }

    @Override
    public ApiResponse<RewardResponse.Clover> getCloverCount(Long userId) {
        return ApiResponse.success(new RewardResponse.Clover(23L));
    }
}
