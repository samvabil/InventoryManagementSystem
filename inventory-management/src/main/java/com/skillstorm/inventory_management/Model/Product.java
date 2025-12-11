/**
 * Represents a catalog product that can be stored in one or more warehouses.
 * Product data is defined once per SKU.
 * Mapped to the PRODUCTS table in the database.
 */
package com.skillstorm.inventory_management.Model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUCTS")
public class Product {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column
    private Department category;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private Set<Inventory> inventoryEntries;

    public Product() {
    }

    public Product(String sku, String name, String description, Department category) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Product(int id, String sku, String name, String description, Department category) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Set<Inventory> getInventoryEntries() {
        return inventoryEntries;
    }

    public void setInventoryEntries(Set<Inventory> inventoryEntries) {
        this.inventoryEntries = inventoryEntries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((sku == null) ? 0 : sku.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((inventoryEntries == null) ? 0 : inventoryEntries.hashCode());
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
        Product other = (Product) obj;
        if (id != other.id)
            return false;
        if (sku == null) {
            if (other.sku != null)
                return false;
        } else if (!sku.equals(other.sku))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        if (inventoryEntries == null) {
            if (other.inventoryEntries != null)
                return false;
        } else if (!inventoryEntries.equals(other.inventoryEntries))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Product [id=" + id + ", sku=" + sku + ", name=" + name + ", description=" + description + ", category="
                + category + ", inventoryEntries=" + inventoryEntries + "]";
    }

}
