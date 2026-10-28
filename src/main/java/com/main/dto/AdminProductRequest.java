package com.main.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Body used by the admin panel to create or fully update a product. */
public record AdminProductRequest(
        @NotBlank(message = "Please enter a product ID")
        @Size(max = 40, message = "Product ID must be 40 characters or fewer")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$",
                message = "Use letters, numbers, dots, dashes or underscores only") String sku,

        @NotBlank(message = "Please enter a product name")
        @Size(max = 160, message = "Name must be 160 characters or fewer") String name,

        /** Optional. Generated from the name when blank. */
        @Size(max = 180, message = "Slug must be 180 characters or fewer") String slug,

        @NotBlank(message = "Please enter an image path or URL")
        @Size(max = 500, message = "Image path must be 500 characters or fewer")
        @Pattern(regexp = "^(/|https?://).+", message = "Use a path starting with / or a full http(s) URL") String imageUrl,

        @NotBlank(message = "Please enter a short description")
        @Size(max = 300, message = "Short description must be 300 characters or fewer") String shortDescription,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer") String description,

        @NotNull(message = "Please enter the minimum price")
        @DecimalMin(value = "0.01", message = "Minimum price must be greater than zero")
        @DecimalMax(value = "10000000.00", message = "Minimum price is too large") BigDecimal minPrice,

        @NotNull(message = "Please enter the maximum price")
        @DecimalMin(value = "0.01", message = "Maximum price must be greater than zero")
        @DecimalMax(value = "10000000.00", message = "Maximum price is too large") BigDecimal maxPrice,

        @NotNull(message = "Please enter the minimum order quantity")
        @Min(value = 1, message = "Minimum order quantity must be at least 1")
        @Max(value = 1000000, message = "Minimum order quantity is too large") Integer minOrderQuantity,

        boolean featured,

        @NotNull(message = "Please enter a display order")
        @Min(value = 0, message = "Display order cannot be negative")
        @Max(value = 100000, message = "Display order is too large") Integer displayOrder,

        /** Optional. Defaults to true on create and is left unchanged on update when omitted. */
        Boolean active,

        @NotNull(message = "Please choose a category") Long categoryId,

        List<Long> occasionIds,

        @Size(max = 20, message = "A maximum of 20 customisation options is allowed")
        List<@NotBlank(message = "Customisation options cannot be blank")
             @Size(max = 100, message = "Each customisation option must be 100 characters or fewer") String> customisationOptions) {
}
