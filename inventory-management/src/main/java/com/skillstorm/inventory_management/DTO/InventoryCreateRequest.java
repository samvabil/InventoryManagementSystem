package com.skillstorm.inventory_management.DTO;

import com.skillstorm.inventory_management.Model.Department;

/**
 * DTO for creating a new Inventory row in a warehouse.
 */
public class InventoryCreateRequest {

    private String sku;
    private String name;
    private String description;
    private Department category;
    private int quantity;
    private String storageLocation;

    public InventoryCreateRequest() {
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}
