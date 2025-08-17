package com.runky.crew.api;

import jakarta.validation.constraints.NotNull;

public class CrewRequest {
    public record Create(
            @NotNull(message = "크루 이름은 필수입니다.")
            String name
    ) {
    }

    public record Join(
            @NotNull(message = "크루 코드는 필수입니다.")
            String code
    ) {
    }

    public record Leave(
            Long newLeaderId
    ) {
    }

    public record Notice(
            String notice
    ) {
    }

    public record Name(
            String name
    ) {
    }

    public record Delegate(
            Long newLeaderId
    ) {
    }

    public record Ban(
            Long targetId
    ) {
    }

    private CrewRequest() {
    }
}
