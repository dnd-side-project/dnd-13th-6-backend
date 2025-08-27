package com.runky.reward.domain;

import com.runky.global.entity.BaseTimeEntity;
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
@Table(name = "clover")
public class Clover extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "count", nullable = false)
    private Long count;

    protected Clover() {
    }

    private Clover(Long userId, Long count) {
        this.userId = userId;
        this.count = count;
    }

    public static Clover of(Long userId) {
        return new Clover(userId, 0L);
    }

    public void add(Long count) {
        if (count < 0) {
            throw new GlobalException(RewardErrorCode.INVALID_CLOVER_ADD_REQUEST);
        }
        this.count += count;
    }

    public void use(Long count) {
        if (this.count < count) {
            throw new GlobalException(RewardErrorCode.INSUFFICIENT_CLOVER);
        }
        this.count -= count;
    }

    public void useForGotcha() {
        if (this.count < 10) {
            throw new GlobalException(RewardErrorCode.INSUFFICIENT_CLOVER);
        }
        this.count -= 10;
    }
}
