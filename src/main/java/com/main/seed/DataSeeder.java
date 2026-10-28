package com.main.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.entity.Category;
import com.main.entity.Occasion;
import com.main.entity.Product;
import com.main.repository.CategoryRepository;
import com.main.repository.OccasionRepository;
import com.main.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;
    private final OccasionRepository occasionRepository;
    private final ProductRepository productRepository;

    public DataSeeder(ObjectMapper objectMapper, CategoryRepository categoryRepository,
                      OccasionRepository occasionRepository, ProductRepository productRepository) {
        this.objectMapper = objectMapper;
        this.categoryRepository = categoryRepository;
        this.occasionRepository = occasionRepository;
        this.productRepository = productRepository;
    }

    record SeedFile(List<SeedCategory> categories, List<SeedOccasion> occasions, List<SeedProduct> products) {
    }

    record SeedCategory(String name, String slug) {
    }

    record SeedOccasion(String name, String slug, String description) {
    }

    record SeedProduct(String sku, String name, String slug, String category, String image, boolean featured,
                       int displayOrder, String shortDescription, String description, BigDecimal minPrice,
                       BigDecimal maxPrice, int minOrderQuantity, List<String> occasions,
                       List<String> customisationOptions) {
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        SeedFile seed;
        try (InputStream in = new ClassPathResource("seed/catalogue.json").getInputStream()) {
            seed = objectMapper.readValue(in, SeedFile.class);
        }

        Map<String, Category> categories = new HashMap<>();
        for (SeedCategory c : seed.categories()) {
            categories.put(c.slug(), categoryRepository.findBySlug(c.slug())
                    .orElseGet(() -> categoryRepository.save(new Category(c.name(), c.slug()))));
        }

        Map<String, Occasion> occasions = new HashMap<>();
        for (SeedOccasion o : seed.occasions()) {
            occasions.put(o.slug(), occasionRepository.findBySlug(o.slug())
                    .orElseGet(() -> occasionRepository.save(new Occasion(o.name(), o.slug(), o.description()))));
        }

        int added = 0;
        for (SeedProduct p : seed.products()) {
            if (productRepository.existsBySku(p.sku())) {
                continue;
            }
            Product product = new Product(p.sku(), p.name(), p.slug(), p.image(), p.shortDescription(),
                    p.description(), p.minPrice(), p.maxPrice(), p.minOrderQuantity(), p.featured(),
                    p.displayOrder(), categories.get(p.category()));
            p.occasions().forEach(slug -> product.getOccasions().add(occasions.get(slug)));
            product.getCustomisationOptions().addAll(p.customisationOptions());
            productRepository.save(product);
            added++;
        }
        log.info("Catalogue seed complete: {} new product(s) added, {} total", added, productRepository.count());
    }
}
