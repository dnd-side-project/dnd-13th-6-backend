package com.dnd13.runners_server.global.response;

import org.springframework.http.HttpStatus;

public interface ResponseCode {
	HttpStatus getStatus();

	String getCode();

	String getMessage();
}
