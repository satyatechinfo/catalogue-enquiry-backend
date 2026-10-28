package com.main.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String sku;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(nullable = false, length = 180)
	private String slug;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "short_description", nullable = false, length = 300)
	private String shortDescription;

	@Column(name = "description", length = 2000)
	private String description;

	@Column(name = "min_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal minPrice;

	@Column(name = "max_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal maxPrice;

	@Column(name = "min_order_quantity", nullable = false)
	private int minOrderQuantity = 1;

	@Column(nullable = false)
	private boolean featured;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToMany
	@JoinTable(name = "product_occasions", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "occasion_id"))
	@BatchSize(size = 50)
	private Set<Occasion> occasions = new LinkedHashSet<>();

	@ElementCollection
	@CollectionTable(name = "product_customisation_options", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "option_name", length = 100)
	@BatchSize(size = 50)
	private List<String> customisationOptions = new ArrayList<>();

	protected Product() {
	}

	public Product(String sku, String name, String slug, String imageUrl, String shortDescription, String description,
			BigDecimal minPrice, BigDecimal maxPrice, int minOrderQuantity, boolean featured, int displayOrder,
			Category category) {
		this.sku = sku;
		this.name = name;
		this.slug = slug;
		this.imageUrl = imageUrl;
		this.shortDescription = shortDescription;
		this.description = description;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.minOrderQuantity = minOrderQuantity;
		this.featured = featured;
		this.displayOrder = displayOrder;
		this.category = category;
	}

	public void updateDetails(String sku, String name, String slug, String imageUrl, String shortDescription,
			String description, BigDecimal minPrice, BigDecimal maxPrice, int minOrderQuantity, boolean featured,
			int displayOrder, Category category) {
		this.sku = sku;
		this.name = name;
		this.slug = slug;
		this.imageUrl = imageUrl;
		this.shortDescription = shortDescription;
		this.description = description;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.minOrderQuantity = minOrderQuantity;
		this.featured = featured;
		this.displayOrder = displayOrder;
		this.category = category;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public String getSku() {
		return sku;
	}

	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getMinPrice() {
		return minPrice;
	}

	public BigDecimal getMaxPrice() {
		return maxPrice;
	}

	public int getMinOrderQuantity() {
		return minOrderQuantity;
	}

	public boolean isFeatured() {
		return featured;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public boolean isActive() {
		return active;
	}

	public Category getCategory() {
		return category;
	}

	public Set<Occasion> getOccasions() {
		return occasions;
	}

	public List<String> getCustomisationOptions() {
		return customisationOptions;
	}
}
