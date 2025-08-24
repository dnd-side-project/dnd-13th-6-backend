package com.runky.reward.domain;

import com.runky.global.error.GlobalException;
import com.runky.reward.error.RewardErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "badge")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "name", nullable = false)
    private String name;

    protected Badge() {
    }

    private Badge(Long id, String imageUrl, String name) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    public static Badge of(String imageUrl, String name) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new GlobalException(RewardErrorCode.INVALID_BADGE_IMAGE_URL);
        }
        if (name == null || name.isBlank()) {
            throw new GlobalException(RewardErrorCode.INVALID_BADGE_NAME);
        }
        return new Badge(null, imageUrl, name);
    }

    public UserBadge issue(Long userId) {
        return UserBadge.of(userId, this.id);
    }
}
