package com.main.util;

import com.main.dto.CategoryDto;
import com.main.dto.EnquiryItemDto;
import com.main.dto.EnquiryResponse;
import com.main.dto.OccasionDto;
import com.main.dto.ProductDto;
import com.main.entity.Category;
import com.main.entity.Enquiry;
import com.main.entity.Occasion;
import com.main.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;


@Component
public class DtoMapper {

    private final String assetsBaseUrl;

    public DtoMapper(@Value("${app.assets.base-url:}") String assetsBaseUrl) {
        this.assetsBaseUrl = assetsBaseUrl == null ? "" : assetsBaseUrl.replaceAll("/+$", "");
    }

    public CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getSlug());
    }

    public OccasionDto toDto(Occasion o) {
        return new OccasionDto(o.getId(), o.getName(), o.getSlug(), o.getDescription());
    }

    public ProductDto toDto(Product p) {
        List<OccasionDto> occasions = p.getOccasions().stream()
                .sorted(Comparator.comparing(Occasion::getId))
                .map(this::toDto)
                .toList();
        return new ProductDto(
                p.getId(), p.getSku(), p.getName(), p.getSlug(), resolveImage(p.getImageUrl()),
                toDto(p.getCategory()), p.getShortDescription(), p.getDescription(),
                p.getMinPrice(), p.getMaxPrice(), p.getMinOrderQuantity(), p.isFeatured(),
                occasions, List.copyOf(p.getCustomisationOptions()));
    }

    public EnquiryResponse toResponse(Enquiry e) {
        List<EnquiryItemDto> items = e.getItems().stream()
                .map(i -> new EnquiryItemDto(i.getProduct().getId(), i.getProductSku(), i.getProductName(), i.getQuantity()))
                .toList();
        return new EnquiryResponse(
                e.getId(), e.getReferenceNumber(), e.getStatus(), e.getSource(), e.getCreatedAt(),
                e.getCustomerName(), e.getCompanyName(), e.getPhone(), e.getEmail(), e.getOccasion(),
                e.getUnits(), e.getBudgetPerUnit(), e.getExpectedDeliveryDate(),
                e.getCustomisationRequired(), e.getAdditionalMessage(), items);
    }

    private String resolveImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        String path = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return assetsBaseUrl + path;
    }
}
