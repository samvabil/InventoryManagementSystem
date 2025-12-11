/**
 * Represents an individual inventory item stored inside a warehouse
 * Includes item name, SKU, quantity, and storage location, which represents the grocery department
 * Mapped to the ITEMS table in the database
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

@Entity
@Table(name = "ITEMS")
public class Item {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "item_name")
    private String name;

    @Column 
    private String sku;

    @Column
    private int quantity;

    @Column(name = "storage_location")
    private String storageLocation;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    public Item() {
    }

    public Item(String name, String sku, int quantity, String storageLocation, Warehouse warehouse) {
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.warehouse = warehouse;
    }

    public Item(int id, String name, String sku, int quantity, String storageLocation, Warehouse warehouse) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.warehouse = warehouse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((sku == null) ? 0 : sku.hashCode());
        result = prime * result + quantity;
        result = prime * result + ((storageLocation == null) ? 0 : storageLocation.hashCode());
        result = prime * result + ((warehouse == null) ? 0 : warehouse.hashCode());
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
        Item other = (Item) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (sku == null) {
            if (other.sku != null)
                return false;
        } else if (!sku.equals(other.sku))
            return false;
        if (quantity != other.quantity)
            return false;
        if (storageLocation == null) {
            if (other.storageLocation != null)
                return false;
        } else if (!storageLocation.equals(other.storageLocation))
            return false;
        if (warehouse == null) {
            if (other.warehouse != null)
                return false;
        } else if (!warehouse.equals(other.warehouse))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Item [id=" + id + ", name=" + name + ", sku=" + sku + ", quantity=" + quantity + ", storageLocation="
                + storageLocation + ", warehouse=" + warehouse + "]";
    }
}
