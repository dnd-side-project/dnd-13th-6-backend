package com.runky.cheer.infrastructure;

import org.springframework.stereotype.Repository;

import com.runky.cheer.domain.Cheer;
import com.runky.cheer.domain.CheerRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CheerRepositoryImpl implements CheerRepository {
	private final CheerJpaRepository cheerJpaRepository;

	@Override
	public void save(final Cheer cheer) {
		cheerJpaRepository.save(cheer);
	}
}
