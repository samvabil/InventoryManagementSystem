package com.skillstorm.inventory_management.DTO;

/**
 * DTO for updating an existing Inventory row's quantity and storage location.
 */
public class InventoryUpdateRequest {

    private int quantity;
    private String storageLocation;

    public InventoryUpdateRequest() {
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
