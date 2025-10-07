package com.runky.running.interfaces.websocket;

public record RoomEvent(String type, Long runningId, Long runnerId, Double x, Double y, Long timestamp) {
}
