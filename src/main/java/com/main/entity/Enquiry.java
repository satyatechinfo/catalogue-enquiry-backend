package com.main.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.BatchSize;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "enquiries")
public class Enquiry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "reference_number", nullable = false, unique = true, length = 32)
	private String referenceNumber;

	@Column(name = "client_request_id", unique = true, length = 64)
	private String clientRequestId;

	@Column(name = "customer_name", nullable = false, length = 120)
	private String customerName;

	@Column(name = "company_name", nullable = false, length = 160)
	private String companyName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false, length = 160)
	private String email;

	@Column(nullable = false, length = 100)
	private String occasion;

	@Column(nullable = false)
	private int units;

	@Column(name = "budget_per_unit", nullable = false, precision = 10, scale = 2)
	private BigDecimal budgetPerUnit;

	@Column(name = "expected_delivery_date", nullable = false)
	private LocalDate expectedDeliveryDate;

	@Column(name = "customisation_required", length = 1000)
	private String customisationRequired;

	@Column(name = "additional_message", length = 2000)
	private String additionalMessage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EnquirySource source = EnquirySource.WEBSITE;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EnquiryStatus status = EnquiryStatus.NEW;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 50)
	private List<EnquiryItem> items = new ArrayList<>();

	protected Enquiry() {
	}

	public Enquiry(String referenceNumber, String clientRequestId, String customerName, String companyName,
			String phone, String email, String occasion, int units, BigDecimal budgetPerUnit,
			LocalDate expectedDeliveryDate, String customisationRequired, String additionalMessage,
			EnquirySource source) {
		this.referenceNumber = referenceNumber;
		this.clientRequestId = clientRequestId;
		this.customerName = customerName;
		this.companyName = companyName;
		this.phone = phone;
		this.email = email;
		this.occasion = occasion;
		this.units = units;
		this.budgetPerUnit = budgetPerUnit;
		this.expectedDeliveryDate = expectedDeliveryDate;
		this.customisationRequired = customisationRequired;
		this.additionalMessage = additionalMessage;
		this.source = source;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void addItem(EnquiryItem item) {
		item.setEnquiry(this);
		items.add(item);
	}

	public Long getId() {
		return id;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public String getClientRequestId() {
		return clientRequestId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public String getOccasion() {
		return occasion;
	}

	public int getUnits() {
		return units;
	}

	public BigDecimal getBudgetPerUnit() {
		return budgetPerUnit;
	}

	public LocalDate getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}

	public String getCustomisationRequired() {
		return customisationRequired;
	}

	public String getAdditionalMessage() {
		return additionalMessage;
	}

	public EnquirySource getSource() {
		return source;
	}

	public EnquiryStatus getStatus() {
		return status;
	}

	public void setStatus(EnquiryStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<EnquiryItem> getItems() {
		return items;
	}
}
