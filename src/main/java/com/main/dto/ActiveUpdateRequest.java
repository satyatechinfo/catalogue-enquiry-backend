package com.main.dto;

import jakarta.validation.constraints.NotNull;

public record ActiveUpdateRequest(@NotNull(message = "Active flag is required") Boolean active) {
}
