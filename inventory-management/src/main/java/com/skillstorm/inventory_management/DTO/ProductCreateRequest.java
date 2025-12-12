package com.skillstorm.inventory_management.DTO;

import com.skillstorm.inventory_management.Model.Department;

/**
 * DTO for creating or looking up a Product by SKU.
 */
public class ProductCreateRequest {

    private String sku;
    private String name;
    private String description;
    private Department category; // HEB department

    public ProductCreateRequest() {
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Department getCategory() {
        return category;
    }

    public void setCategory(Department category) {
        this.category = category;
    }
}
