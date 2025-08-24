package com.runky.member.domain;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class Nickname {

    @Column(name = "nickname", nullable = false, unique = true)
    private String value;

    protected Nickname() {
    }

    public Nickname(String value) {
        if (value == null || value.isBlank()) {
            throw new GlobalException(MemberErrorCode.BLANK_NICKNAME);
        }
        if (value.length() > 10) {
            throw new GlobalException(MemberErrorCode.OVER_LENGTH_NICKNAME);
        }
        if (!value.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new GlobalException(MemberErrorCode.INVALID_FORMAT_NICKNAME);
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
