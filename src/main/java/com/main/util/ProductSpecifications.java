package com.main.util;

import com.main.entity.Occasion;
import com.main.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> filter(String categorySlug, String occasionSlug, BudgetBand band,
                                                String search, Boolean featured) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (StringUtils.hasText(categorySlug)) {
                predicates.add(cb.equal(root.join("category").get("slug"), categorySlug.trim().toLowerCase(Locale.ROOT)));
            }

            if (StringUtils.hasText(occasionSlug)) {
                // EXISTS sub-query avoids duplicate rows and keeps paging counts correct.
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Product> subRoot = sub.from(Product.class);
                Join<Product, Occasion> occasionJoin = subRoot.join("occasions");
                sub.select(subRoot.get("id")).where(
                        cb.equal(subRoot.get("id"), root.get("id")),
                        cb.equal(occasionJoin.get("slug"), occasionSlug.trim().toLowerCase(Locale.ROOT)));
                predicates.add(cb.exists(sub));
            }

            if (band != null) {
                if (band.upper() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("minPrice"), band.upper()));
                }
                if (band.lower() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("maxPrice"), band.lower()));
                }
            }

            if (StringUtils.hasText(search)) {
                String like = "%" + escapeLike(search.trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like, '\\'),
                        cb.like(cb.lower(root.get("shortDescription")), like, '\\'),
                        cb.like(cb.lower(root.get("sku")), like, '\\')));
            }

            if (featured != null) {
                predicates.add(cb.equal(root.get("featured"), featured));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
