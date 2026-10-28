package com.main.service;

import com.main.dto.CategoryDto;
import com.main.dto.OccasionDto;
import com.main.dto.PageResponse;
import com.main.dto.ProductDto;
import com.main.entity.Product;
import com.main.exception.ResourceNotFoundException;
import com.main.repository.CategoryRepository;
import com.main.repository.OccasionRepository;
import com.main.repository.ProductRepository;
import com.main.util.BudgetBand;
import com.main.util.DtoMapper;
import com.main.util.ProductSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogueService {

    private static final Logger log = LoggerFactory.getLogger(CatalogueService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OccasionRepository occasionRepository;
    private final DtoMapper mapper;

    public CatalogueService(ProductRepository productRepository, CategoryRepository categoryRepository,
                            OccasionRepository occasionRepository, DtoMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.occasionRepository = occasionRepository;
        this.mapper = mapper;
    }

    public PageResponse<ProductDto> searchProducts(String category, String occasion, String budget, String search,
                                                   Boolean featured, int page, int size, String sort) {
        BudgetBand band = StringUtils.hasText(budget) ? BudgetBand.fromCode(budget.trim()) : null;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sortFor(sort));
        log.debug("Product search category={} occasion={} budget={} search={} sort={} page={}",
                category, occasion, budget, search, sort, page);
        return PageResponse.of(
                productRepository.findAll(ProductSpecifications.filter(category, occasion, band, search, featured), pageable),
                mapper::toDto);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " was not found."));
        return mapper.toDto(product);
    }

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll(Sort.by("id")).stream().map(mapper::toDto).toList();
    }

    public List<OccasionDto> getOccasions() {
        return occasionRepository.findAll(Sort.by("id")).stream().map(mapper::toDto).toList();
    }

    private Sort sortFor(String sort) {
        String key = sort == null ? "featured" : sort.trim().toLowerCase();
        return switch (key) {
            case "price-asc" -> Sort.by(Sort.Order.asc("minPrice"), Sort.Order.asc("id"));
            case "price-desc" -> Sort.by(Sort.Order.desc("maxPrice"), Sort.Order.asc("id"));
            case "name-asc" -> Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));
            case "newest" -> Sort.by(Sort.Order.desc("id"));
            default -> Sort.by(Sort.Order.desc("featured"), Sort.Order.asc("displayOrder"), Sort.Order.asc("id"));
        };
    }
}
