package com.runky.notification.domain.push;

import static com.runky.notification.domain.aggregate.PushInfo.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.global.error.GlobalException;
import com.runky.notification.domain.aggregate.PushCommand;
import com.runky.notification.error.NotificationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

	private final DeviceTokenRepository deviceTokenRepository;

	@Transactional
	public void register(PushCommand.RegisterDeviceToken command) {
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
	public DeletionDTResult delete(PushCommand.DeleteDeviceToken command) {
		int deletedCount = deviceTokenRepository.deleteByMemberIdAndToken(command.memberId(), command.token());

		if (deletedCount == 0) {
			throw new GlobalException(NotificationErrorCode.NOT_EXIST_TO_DELETE_DEVICE_TOKEN);
		}
		return new DeletionDTResult(deletedCount);
	}

	@Transactional(readOnly = true)
	public ActiveDeviceToken getActiveToken(PushCommand.GetDeviceToken cmd) {
		return deviceTokenRepository.findByMemberId(cmd.memberId())
			.map(dt -> new ActiveDeviceToken(dt.getToken()))
			.orElseThrow(() -> new GlobalException(NotificationErrorCode.NOT_FOUND_DEVICE_TOKEN));
	}

	@Transactional(readOnly = true)
	public ActiveDeviceTokens getActiveTokens(PushCommand.GetAllDeviceToken command) {
		if (command.memberIds().isEmpty()) {
			throw new GlobalException(NotificationErrorCode.EMTPY_TOKEN_OWNER_IDS);
		}
		return new ActiveDeviceTokens(deviceTokenRepository.findActiveTokensByMemberIds(command.memberIds()));
	}

	@Transactional(readOnly = true)
	public DeviceExistenceCheck isExists(PushCommand.CheckDTExistence command) {
		boolean exists = deviceTokenRepository.existsActiveByMemberId(command.memberId());
		return new DeviceExistenceCheck((exists));
	}

	private boolean isUniqueViolationOnToken(DataIntegrityViolationException e) {
		Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
		String msg = (root != null ? root.getMessage() : e.getMessage());
		return msg != null && msg.contains("ux_device_token_token");
	}

}
