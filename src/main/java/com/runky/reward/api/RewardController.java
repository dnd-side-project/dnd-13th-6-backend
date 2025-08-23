package com.runky.reward.api;

import com.runky.global.response.ApiResponse;
import com.runky.reward.application.RewardCriteria;
import com.runky.reward.application.RewardFacade;
import com.runky.reward.application.RewardResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardController implements RewardApiSpec {

    private final RewardFacade rewardFacade;

    @Override
    @GetMapping("/badges")
    public ApiResponse<RewardResponse.Images> getBadges(@RequestHeader("X-USER-ID") Long userId) {
        List<RewardResult.Badge> results = rewardFacade.getMyBadges(new RewardCriteria.Find(userId));
        RewardResponse.Images response = new RewardResponse.Images(results.stream()
                .map(result -> new RewardResponse.Image(result.ImageUrl()))
                .toList());
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<RewardResponse.Draw> drawBadge(Long userId) {
        return ApiResponse.success(new RewardResponse.Draw("/badge12.png"));
    }

    @Override
    @GetMapping("/clovers")
    public ApiResponse<RewardResponse.Clover> getCloverCount(@RequestHeader("X-USER-ID") Long userId) {
        RewardResult.Clover result = rewardFacade.getClover(new RewardCriteria.Find(userId));
        return ApiResponse.success(new RewardResponse.Clover(result.count()));
    }
}
