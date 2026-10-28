package com.main.controller;

import com.main.dto.CategoryDto;
import com.main.dto.OccasionDto;
import com.main.dto.PageResponse;
import com.main.dto.ProductDto;
import com.main.service.CatalogueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    
    @GetMapping("/products")
    public PageResponse<ProductDto> products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String occasion,
            @RequestParam(required = false) String budget,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String sort) {
        return catalogueService.searchProducts(category, occasion, budget, search, featured, page, size, sort);
    }

    @GetMapping("/products/{id}")
    public ProductDto product(@PathVariable Long id) {
        return catalogueService.getProduct(id);
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return catalogueService.getCategories();
    }

    @GetMapping("/occasions")
    public List<OccasionDto> occasions() {
        return catalogueService.getOccasions();
    }
}
