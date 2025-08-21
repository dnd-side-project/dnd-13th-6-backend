package com.runky.notification.domain.push;

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
	public void register(DeviceTokenCommand.Register command) {
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
	public DeviceTokenInfo.Delete delete(DeviceTokenCommand.Delete command) {
		int deletedCount = deviceTokenRepository.deleteByMemberIdAndToken(command.memberId(), command.token());

		if (deletedCount == 0) {
			throw new GlobalException(NotificationErrorCode.NOT_EXIST_TO_DELETE_DEVICE_TOKEN);
		}
		return new DeviceTokenInfo.Delete(deletedCount);
	}

	@Transactional(readOnly = true)
	public DeviceTokenInfo.View getDeviceToken(DeviceTokenCommand.Get cmd) {
		return deviceTokenRepository.findByMemberIdAndDeviceType(cmd.memberId(), cmd.deviceType())
			.map(dt -> new DeviceTokenInfo.View(dt.getId(), dt.getMemberId(), dt.getToken(), dt.isActive()))
			.orElseThrow(() -> new GlobalException(NotificationErrorCode.NOT_FOUND_DEVICE_TOKEN));
	}

	@Transactional(readOnly = true)
	public DeviceTokenInfo.Existence isExists(DeviceTokenCommand.Existence command) {
		boolean exists = deviceTokenRepository.existsActiveByMemberIdAndDeviceType(command.memberId(),
			command.deviceType());
		return new DeviceTokenInfo.Existence((exists));
	}

	private boolean isUniqueViolationOnToken(DataIntegrityViolationException e) {
		Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
		String msg = (root != null ? root.getMessage() : e.getMessage());
		return msg != null && msg.contains("ux_device_token_token");
	}

}
