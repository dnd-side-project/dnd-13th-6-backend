package com.runky.running.interfaces.websocket;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record LocationMessage(
	@NotNull @DecimalMin(value = "-180", inclusive = true) @DecimalMax(value = "180", inclusive = true)
	Double x,
	@NotNull @DecimalMin(value = "-90", inclusive = true) @DecimalMax(value = "90", inclusive = true)
	Double y,
	@PositiveOrZero
	Long timestamp
) {
}
