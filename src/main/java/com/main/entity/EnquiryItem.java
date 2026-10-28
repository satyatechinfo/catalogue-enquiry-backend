package com.main.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "enquiry_items")
public class EnquiryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @Column(name = "product_sku", nullable = false, length = 40)
    private String productSku;

    @Column(nullable = false)
    private int quantity;

    protected EnquiryItem() {
    }

    public EnquiryItem(Product product, int quantity) {
        this.product = product;
        this.productName = product.getName();
        this.productSku = product.getSku();
        this.quantity = quantity;
    }

    void setEnquiry(Enquiry enquiry) { this.enquiry = enquiry; }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public String getProductSku() { return productSku; }
    public int getQuantity() { return quantity; }
}
