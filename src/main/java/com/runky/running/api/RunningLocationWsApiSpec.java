package com.runky.running.api;

/**
 * 실시간 런닝 위치 공유 WebSocket API 명세
 */
public interface RunningLocationWsApiSpec {

	/**
	 * 클라이언트로부터 실시간 위치 정보를 수신하여 해당 런닝 세션을 구독하는 모든 클라이언트에게 브로드캐스팅합니다.
	 * <p>
	 * - <b>Publish Destination</b>: /app/runnings/{runningId}/location
	 * <p>
	 * - <b>Subscribe Destination</b>: /topic/runnings/{runningId}
	 *
	 * @param runningId 런닝 세션 ID
	 * @param payload   위치 정보 메시지 (runnerId, x, y, timestamp)
	 * @return 브로드캐스팅될 RoomEvent 객체
	 */
	RunningLocationWsController.RoomEvent publish(
		Long runningId,
		RunningLocationWsController.LocationMessage payload
	);
}
