package com.runky.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.runky.reward.domain.RewardCommand.GetBadges;
import com.runky.utils.DatabaseCleanUp;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RewardServiceIntegrationTest {

    @Autowired
    private RewardService rewardService;
    @Autowired
    private BadgeRepository badgeRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("유저가 소유한 뱃지를 조회한다.")
    void getBadges() {
        Badge badge = badgeRepository.save(Badge.of("image.pvg", "뱃지1"));
        badgeRepository.save(UserBadge.of(1L, badge.getId()));

        List<Badge> badges = rewardService.getBadges(new GetBadges(1L));

        assertThat(badges).hasSize(1);
    }
}