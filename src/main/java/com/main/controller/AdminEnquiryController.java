package com.main.controller;

import com.main.dto.EnquiryResponse;
import com.main.dto.PageResponse;
import com.main.dto.StatusUpdateRequest;
import com.main.entity.EnquiryStatus;
import com.main.service.EnquiryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/enquiries")
public class AdminEnquiryController {

    private final EnquiryService enquiryService;

    public AdminEnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @GetMapping
    public PageResponse<EnquiryResponse> list(@RequestParam(required = false) EnquiryStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return enquiryService.list(status, page, size);
    }

    @GetMapping("/{id}")
    public EnquiryResponse get(@PathVariable Long id) {
        return enquiryService.get(id);
    }

    @PatchMapping("/{id}/status")
    public EnquiryResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return enquiryService.updateStatus(id, request.status());
    }
}
