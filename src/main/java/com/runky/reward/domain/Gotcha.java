package com.runky.reward.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gotcha {
    DEFAULT {
        @Override
        public Capsule random() {
            List<Capsule> capsules = new ArrayList<>();
            for (Capsule capsule : Gotcha.Capsule.values()) {
                int count = capsule.rate.multiply(BigDecimal.valueOf(100)).intValue();
                for (int i = 0; i < count; i++) {
                    capsules.add(capsule);
                }
            }

            int randomIndex = new Random().nextInt(100);

            return capsules.get(randomIndex);
        }
    };

    @Getter
    @RequiredArgsConstructor
    public enum Capsule {
        BADGE_1("뱃지 1", BigDecimal.valueOf(0.2)),
        BADGE_2("뱃지 2", BigDecimal.valueOf(0.2)),
        BADGE_3("뱃지 3", BigDecimal.valueOf(0.2)),
        BADGE_4("뱃지 4", BigDecimal.valueOf(0.2)),
        BADGE_5("뱃지 5", BigDecimal.valueOf(0.2));

        private final String name;
        private final BigDecimal rate;

    }

    public abstract Capsule random();
}
