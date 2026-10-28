package com.main.dto;

import java.math.BigDecimal;
import java.util.List;

/** Flat product representation for the admin panel. Includes hidden products and the raw image path. */
public record AdminProductDto(
        Long id,
        String sku,
        String name,
        String slug,
        String imageUrl,
        Long categoryId,
        String categoryName,
        String shortDescription,
        String description,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int minOrderQuantity,
        boolean featured,
        int displayOrder,
        boolean active,
        List<Long> occasionIds,
        List<String> customisationOptions) {
}
