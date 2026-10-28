package com.main.repository;

import com.main.entity.Enquiry;
import com.main.entity.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    boolean existsByReferenceNumber(String referenceNumber);

    Optional<Enquiry> findByClientRequestId(String clientRequestId);

    Page<Enquiry> findByStatus(EnquiryStatus status, Pageable pageable);
}
