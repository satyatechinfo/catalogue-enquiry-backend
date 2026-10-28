package com.main.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Long id,
        String sku,
        String name,
        String slug,
        String imageUrl,
        CategoryDto category,
        String shortDescription,
        String description,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int minOrderQuantity,
        boolean featured,
        List<OccasionDto> occasions,
        List<String> customisationOptions) {
}
