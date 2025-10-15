package com.runky.auth.domain;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.domain.token.refresh.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenMaintenanceService {
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public int deleteExpiredUntil(Instant now) {
		return refreshTokenRepository.deleteExpiredBefore(now);
	}
}
