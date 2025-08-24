package com.runky.cheer.domain;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.runky.cheer.error.CheerErrorCode;
import com.runky.global.error.GlobalException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheerService {
	private final CheerRepository cheerRepository;

	public CheerInfo.Detail create(CheerCommand.Create command) {
		try {
			Cheer cheer = Cheer.of(command.runningId(), command.senderId(), command.receiverId(),
				command.message());
			cheerRepository.save(cheer);
			return new CheerInfo.Detail(cheer.getId(), cheer.getRunningId(), cheer.getSenderId(), cheer.getReceiverId(),
				cheer.getCreatedAt().toInstant());
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().contains("Duplicate entry")) {
				throw new GlobalException(CheerErrorCode.ALREADY_DO_CHEER);
			}
			throw e;
		}
	}

}

