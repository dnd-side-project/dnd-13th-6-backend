package com.runky.auth.domain.token.exchange.vo;

import lombok.Builder;

@Builder
public record AuthExchangeData(
	Long memberId,
	Long createdAt
) {
}
