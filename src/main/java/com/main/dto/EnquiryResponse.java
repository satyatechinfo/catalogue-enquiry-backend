package com.main.dto;

import com.main.entity.EnquirySource;
import com.main.entity.EnquiryStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record EnquiryResponse(
        Long id,
        String referenceNumber,
        EnquiryStatus status,
        EnquirySource source,
        Instant createdAt,
        String customerName,
        String companyName,
        String phone,
        String email,
        String occasion,
        int units,
        BigDecimal budgetPerUnit,
        LocalDate expectedDeliveryDate,
        String customisationRequired,
        String additionalMessage,
        List<EnquiryItemDto> items) {
}
