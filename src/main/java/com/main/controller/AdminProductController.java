package com.main.controller;

import com.main.dto.ActiveUpdateRequest;
import com.main.dto.AdminProductDto;
import com.main.dto.AdminProductRequest;
import com.main.dto.PageResponse;
import com.main.service.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Product management for the admin panel. Protected by HTTP Basic authentication (ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService productService;

    public AdminProductController(AdminProductService productService) {
        this.productService = productService;
    }

    /** Lists every product, including hidden ones. */
    @GetMapping
    public PageResponse<AdminProductDto> list(@RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return productService.list(search, page, size);
    }

    @GetMapping("/{id}")
    public AdminProductDto get(@PathVariable Long id) {
        return productService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductDto create(@Valid @RequestBody AdminProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public AdminProductDto update(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        return productService.update(id, request);
    }

    /** Shows or hides a product in the public catalogue. */
    @PatchMapping("/{id}/active")
    public AdminProductDto setActive(@PathVariable Long id, @Valid @RequestBody ActiveUpdateRequest request) {
        return productService.setActive(id, request.active());
    }
}
