package com.runky.notification.domain.push;

import static com.runky.notification.domain.push.PushInfo.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.global.error.GlobalException;
import com.runky.notification.error.NotificationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

	private final DeviceTokenRepository deviceTokenRepository;

	@Transactional
	public void register(PushCommand.DeviceToken.Register command) {
		DeviceToken deviceToken = DeviceToken.register(command.memberId(), command.token(), command.deviceType());
		try {
			deviceTokenRepository.save(deviceToken);
		} catch (DataIntegrityViolationException e) {
			if (isUniqueViolationOnToken(e)) {
				throw new GlobalException(NotificationErrorCode.DUPLICATE_UNIQUE_KEY_DEVICE_TOKEN);
			}
			throw e;
		}
	}

	@Transactional
	public DeviceTokenInfo.DeletionResult delete(PushCommand.DeviceToken.Delete command) {
		int deletedCount = deviceTokenRepository.deleteByMemberIdAndToken(command.memberId(), command.token());

		if (deletedCount == 0) {
			throw new GlobalException(NotificationErrorCode.NOT_EXIST_TO_DELETE_DEVICE_TOKEN);
		}
		return new DeviceTokenInfo.DeletionResult(deletedCount);
	}

	@Transactional(readOnly = true)
	public DeviceTokenInfo.ActiveToken getActiveToken(PushCommand.DeviceToken.Get cmd) {
		return deviceTokenRepository.findByMemberId(cmd.memberId())
			.map(dt -> new DeviceTokenInfo.ActiveToken(dt.getToken()))
			.orElseThrow(() -> new GlobalException(NotificationErrorCode.NOT_FOUND_DEVICE_TOKEN));
	}

	@Transactional(readOnly = true)
	public DeviceTokenInfo.ActiveTokens getActiveTokens(PushCommand.DeviceToken.Gets command) {
		return new DeviceTokenInfo.ActiveTokens(deviceTokenRepository.findActiveTokensByMemberIds(command.memberIds()));
	}

	@Transactional(readOnly = true)
	public DeviceTokenInfo.ExistenceCheck isExists(PushCommand.DeviceToken.CheckExistence command) {
		boolean exists = deviceTokenRepository.existsActiveByMemberId(command.memberId());
		return new DeviceTokenInfo.ExistenceCheck((exists));
	}

	private boolean isUniqueViolationOnToken(DataIntegrityViolationException e) {
		Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
		String msg = (root != null ? root.getMessage() : e.getMessage());
		return msg != null && msg.contains("ux_device_token_token");
	}

}
