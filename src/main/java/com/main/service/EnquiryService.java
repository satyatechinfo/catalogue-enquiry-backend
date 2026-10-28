package com.main.service;

import com.main.dto.EnquiryItemRequest;
import com.main.dto.EnquiryRequest;
import com.main.dto.EnquiryResponse;
import com.main.dto.PageResponse;
import com.main.entity.Enquiry;
import com.main.entity.EnquiryItem;
import com.main.entity.EnquirySource;
import com.main.entity.EnquiryStatus;
import com.main.entity.Product;
import com.main.exception.BadRequestException;
import com.main.exception.ResourceNotFoundException;
import com.main.repository.EnquiryRepository;
import com.main.repository.ProductRepository;
import com.main.util.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnquiryService {

    private static final Logger log = LoggerFactory.getLogger(EnquiryService.class);
    private static final String REF_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EnquiryRepository enquiryRepository;
    private final ProductRepository productRepository;
    private final DtoMapper mapper;
    private final TransactionTemplate tx;

    public EnquiryService(EnquiryRepository enquiryRepository, ProductRepository productRepository,
                          DtoMapper mapper, TransactionTemplate tx) {
        this.enquiryRepository = enquiryRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
        this.tx = tx;
    }

    /**
     * Persists an enquiry and returns it only after the database has accepted it.
     * Repeating a request with the same clientRequestId returns the original enquiry instead of creating a duplicate.
     */
    public EnquiryResponse create(EnquiryRequest request) {
        String requestId = StringUtils.hasText(request.clientRequestId()) ? request.clientRequestId().trim() : null;

        if (requestId != null) {
            EnquiryResponse existing = findByRequestId(requestId);
            if (existing != null) {
                log.info("Duplicate submission ignored, returning enquiry {}", existing.referenceNumber());
                return existing;
            }
        }

        try {
            EnquiryResponse saved = tx.execute(status -> persist(request, requestId));
            log.info("Enquiry {} stored (source={}, units={}, company={})",
                    saved.referenceNumber(), saved.source(), saved.units(), saved.companyName());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            // Two identical requests raced each other; the unique client_request_id rejected the second one.
            if (requestId != null) {
                EnquiryResponse existing = findByRequestId(requestId);
                if (existing != null) {
                    return existing;
                }
            }
            throw ex;
        }
    }

    public PageResponse<EnquiryResponse> list(EnquiryStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        return tx.execute(s -> PageResponse.of(
                status == null ? enquiryRepository.findAll(pageable) : enquiryRepository.findByStatus(status, pageable),
                mapper::toResponse));
    }

    public EnquiryResponse get(Long id) {
        return tx.execute(s -> mapper.toResponse(enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry " + id + " was not found."))));
    }

    public EnquiryResponse updateStatus(Long id, EnquiryStatus newStatus) {
        return tx.execute(s -> {
            Enquiry enquiry = enquiryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Enquiry " + id + " was not found."));
            enquiry.setStatus(newStatus);
            log.info("Enquiry {} moved to {}", enquiry.getReferenceNumber(), newStatus);
            return mapper.toResponse(enquiry);
        });
    }

    private EnquiryResponse findByRequestId(String requestId) {
        return tx.execute(s -> enquiryRepository.findByClientRequestId(requestId).map(mapper::toResponse).orElse(null));
    }

    private EnquiryResponse persist(EnquiryRequest r, String requestId) {
        Enquiry enquiry = new Enquiry(
                nextReference(), requestId,
                r.customerName().trim(), r.companyName().trim(), normalisePhone(r.phone()),
                r.email().trim().toLowerCase(), r.occasion().trim(), r.units(), r.budgetPerUnit(),
                r.expectedDeliveryDate(), trimToNull(r.customisationRequired()), trimToNull(r.additionalMessage()),
                r.source() == null ? EnquirySource.WEBSITE : r.source());

        for (Map.Entry<Long, Integer> line : mergeItems(r.items()).entrySet()) {
            Product product = productRepository.findById(line.getKey()).orElseThrow(() -> unknownProduct(line.getKey()));
            if (!product.isActive()) {
                throw unknownProduct(line.getKey());
            }
            enquiry.addItem(new EnquiryItem(product, line.getValue()));
        }

        return mapper.toResponse(enquiryRepository.saveAndFlush(enquiry));
    }

    /** Combines repeated product ids so each product appears once with the summed quantity. */
    private Map<Long, Integer> mergeItems(List<EnquiryItemRequest> items) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        if (items != null) {
            for (EnquiryItemRequest item : items) {
                merged.merge(item.productId(), item.quantity(), Integer::sum);
            }
        }
        return merged;
    }

    private BadRequestException unknownProduct(Long productId) {
        return new BadRequestException("One of the selected products is no longer available.",
                Map.of("items", "Product " + productId + " does not exist or is no longer available"));
    }

    private String nextReference() {
        String date = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
        String reference;
        do {
            reference = "TG-" + date + "-" + randomSuffix(6);
        } while (enquiryRepository.existsByReferenceNumber(reference));
        return reference;
    }

    private static String randomSuffix(int length) {
        return RANDOM.ints(length, 0, REF_ALPHABET.length())
                .mapToObj(i -> String.valueOf(REF_ALPHABET.charAt(i)))
                .collect(Collectors.joining());
    }

    /** Stores Indian mobile numbers consistently as +91XXXXXXXXXX. */
    private static String normalisePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        String last10 = digits.substring(digits.length() - 10);
        return "+91" + last10;
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
