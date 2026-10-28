package com.main.dto;

import com.main.entity.EnquirySource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EnquiryRequest(
        @NotBlank(message = "Please enter your name")
        @Size(max = 120, message = "Name must be 120 characters or fewer") String customerName,

        @NotBlank(message = "Please enter your company name")
        @Size(max = 160, message = "Company name must be 160 characters or fewer") String companyName,

        @NotBlank(message = "Please enter a phone number")
        @Pattern(regexp = "^(?:\\+91[- ]?|0)?[6-9]\\d{9}$",
                message = "Enter a valid 10-digit Indian mobile number") String phone,

        @NotBlank(message = "Please enter your email address")
        @Email(message = "Enter a valid email address")
        @Size(max = 160, message = "Email must be 160 characters or fewer") String email,

        @NotBlank(message = "Please select an occasion")
        @Size(max = 100, message = "Occasion must be 100 characters or fewer") String occasion,

        @NotNull(message = "Please enter the number of units")
        @Min(value = 1, message = "Number of units must be at least 1")
        @Max(value = 1000000, message = "Number of units is too large") Integer units,

        @NotNull(message = "Please enter your budget per unit")
        @DecimalMin(value = "1.00", message = "Budget per unit must be greater than zero")
        @DecimalMax(value = "10000000.00", message = "Budget per unit is too large") BigDecimal budgetPerUnit,

        @NotNull(message = "Please choose an expected delivery date")
        @Future(message = "Delivery date must be in the future") LocalDate expectedDeliveryDate,

        @Valid
        @Size(max = 50, message = "A maximum of 50 products can be selected") List<EnquiryItemRequest> items,

        @Size(max = 1000, message = "Customisation details must be 1000 characters or fewer") String customisationRequired,

        @Size(max = 2000, message = "Message must be 2000 characters or fewer") String additionalMessage,

        
        EnquirySource source,

       
        @Size(max = 64, message = "Request id must be 64 characters or fewer") String clientRequestId) {
}
