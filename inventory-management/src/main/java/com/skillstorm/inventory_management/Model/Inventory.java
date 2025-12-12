/**
 * Junction entity that links a Product to a Warehouse.
 * Tracks how much of that product is stored in the warehouse
 * and where inside the warehouse it is located.
 * Mapped to the INVENTORY table in the database.
 */
package com.skillstorm.inventory_management.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "INVENTORY",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "warehouse_id", "product_id" })
    }
)
public class Inventory {

    @Id
    @Column(name = "inventory_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "storage_location")
    private String storageLocation;

    public Inventory() {
    }

    public Inventory(Warehouse warehouse, Product product, int quantity, String storageLocation) {
        this.warehouse = warehouse;
        this.product = product;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
    }

    public Inventory(int id, Warehouse warehouse, Product product, int quantity, String storageLocation) {
        this.id = id;
        this.warehouse = warehouse;
        this.product = product;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((warehouse == null) ? 0 : warehouse.hashCode());
        result = prime * result + ((product == null) ? 0 : product.hashCode());
        result = prime * result + quantity;
        result = prime * result + ((storageLocation == null) ? 0 : storageLocation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Inventory other = (Inventory) obj;
        if (id != other.id)
            return false;
        if (warehouse == null) {
            if (other.warehouse != null)
                return false;
        } else if (!warehouse.equals(other.warehouse))
            return false;
        if (product == null) {
            if (other.product != null)
                return false;
        } else if (!product.equals(other.product))
            return false;
        if (quantity != other.quantity)
            return false;
        if (storageLocation == null) {
            if (other.storageLocation != null)
                return false;
        } else if (!storageLocation.equals(other.storageLocation))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Inventory [id=" + id + ", warehouse=" + warehouse + ", product=" + product + ", quantity=" + quantity
                + ", storageLocation=" + storageLocation + "]";
    }
    
}
