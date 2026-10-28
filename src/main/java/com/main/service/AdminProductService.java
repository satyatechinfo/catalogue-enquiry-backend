package com.main.service;

import com.main.dto.AdminProductDto;
import com.main.dto.AdminProductRequest;
import com.main.dto.PageResponse;
import com.main.entity.Category;
import com.main.entity.Occasion;
import com.main.entity.Product;
import com.main.exception.BadRequestException;
import com.main.exception.ResourceNotFoundException;
import com.main.repository.CategoryRepository;
import com.main.repository.OccasionRepository;
import com.main.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Back-office product management. Products are never deleted (enquiries reference them); they are hidden
 * instead by setting {@code active = false}, which removes them from the public catalogue.
 */
@Service
public class AdminProductService {

    private static final Logger log = LoggerFactory.getLogger(AdminProductService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OccasionRepository occasionRepository;

    public AdminProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                               OccasionRepository occasionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.occasionRepository = occasionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProductDto> list(String search, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("id")));
        Specification<Product> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String like = "%" + search.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like, '\\'),
                    cb.like(cb.lower(root.get("sku")), like, '\\'));
        };
        return PageResponse.of(productRepository.findAll(spec, pageable), this::toDto);
    }

    @Transactional(readOnly = true)
    public AdminProductDto get(Long id) {
        return toDto(find(id));
    }

    @Transactional
    public AdminProductDto create(AdminProductRequest request) {
        Resolved resolved = resolve(request, null);
        Product product = new Product(resolved.sku(), request.name().trim(), resolved.slug(),
                request.imageUrl().trim(), request.shortDescription().trim(), resolved.description(),
                request.minPrice(), request.maxPrice(), request.minOrderQuantity(), request.featured(),
                request.displayOrder(), resolved.category());
        product.setActive(request.active() == null || request.active());
        product.getOccasions().addAll(resolved.occasions());
        product.getCustomisationOptions().addAll(resolved.options());
        Product saved = productRepository.save(product);
        log.info("Admin created product {} ({})", saved.getSku(), saved.getId());
        return toDto(saved);
    }

    @Transactional
    public AdminProductDto update(Long id, AdminProductRequest request) {
        Product product = find(id);
        Resolved resolved = resolve(request, product);
        product.updateDetails(resolved.sku(), request.name().trim(), resolved.slug(), request.imageUrl().trim(),
                request.shortDescription().trim(), resolved.description(), request.minPrice(), request.maxPrice(),
                request.minOrderQuantity(), request.featured(), request.displayOrder(), resolved.category());
        if (request.active() != null) {
            product.setActive(request.active());
        }
        product.getOccasions().clear();
        product.getOccasions().addAll(resolved.occasions());
        product.getCustomisationOptions().clear();
        product.getCustomisationOptions().addAll(resolved.options());
        log.info("Admin updated product {} ({})", product.getSku(), product.getId());
        return toDto(product);
    }

    @Transactional
    public AdminProductDto setActive(Long id, boolean active) {
        Product product = find(id);
        product.setActive(active);
        log.info("Admin {} product {} ({})", active ? "published" : "hid", product.getSku(), product.getId());
        return toDto(product);
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " was not found."));
    }

    private record Resolved(String sku, String slug, String description, Category category,
                            List<Occasion> occasions, List<String> options) {
    }

    /** Validates the cross-field and database-dependent rules that annotations cannot express. */
    private Resolved resolve(AdminProductRequest r, Product existing) {
        Map<String, String> errors = new LinkedHashMap<>();

        String sku = r.sku().trim().toUpperCase(Locale.ROOT);
        boolean skuChanged = existing == null || !existing.getSku().equalsIgnoreCase(sku);
        if (skuChanged && productRepository.existsBySku(sku)) {
            errors.put("sku", "This product ID is already used by another product");
        }

        if (r.maxPrice().compareTo(r.minPrice()) < 0) {
            errors.put("maxPrice", "Maximum price cannot be lower than the minimum price");
        }

        Category category = categoryRepository.findById(r.categoryId()).orElse(null);
        if (category == null) {
            errors.put("categoryId", "The selected category does not exist");
        }

        List<Long> occasionIds = r.occasionIds() == null ? List.of()
                : r.occasionIds().stream().filter(Objects::nonNull).distinct().toList();
        List<Occasion> occasions = occasionIds.isEmpty() ? List.of() : occasionRepository.findAllById(occasionIds);
        if (occasions.size() != occasionIds.size()) {
            errors.put("occasionIds", "One or more selected occasions do not exist");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException("Please correct the highlighted fields.", errors);
        }

        List<String> options = r.customisationOptions() == null ? List.of()
                : r.customisationOptions().stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        String slug = slugify(StringUtils.hasText(r.slug()) ? r.slug() : r.name());
        String description = StringUtils.hasText(r.description()) ? r.description().trim() : null;
        return new Resolved(sku, slug, description, category, occasions, options);
    }

    static String slugify(String text) {
        String slug = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            slug = "product";
        }
        return slug.length() > 180 ? slug.substring(0, 180).replaceAll("-+$", "") : slug;
    }

    private AdminProductDto toDto(Product p) {
        return new AdminProductDto(
                p.getId(), p.getSku(), p.getName(), p.getSlug(), p.getImageUrl(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getShortDescription(), p.getDescription(), p.getMinPrice(), p.getMaxPrice(),
                p.getMinOrderQuantity(), p.isFeatured(), p.getDisplayOrder(), p.isActive(),
                p.getOccasions().stream().map(Occasion::getId).sorted().toList(),
                List.copyOf(p.getCustomisationOptions()));
    }
}
