package com.main.dto;

import com.main.entity.EnquiryStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull(message = "Status is required") EnquiryStatus status) {
}
