/**
 * Represents a warehouse facility that stores inventory items
 * Includes name, location, maximum storage capacity
 * Mapped to the WAREHOUSES table in the database
 */
package com.skillstorm.inventory_management.Model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "WAREHOUSES")
public class Warehouse {
    
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "warehouse_name")
    private String name;

    @Column(name = "warehouse_location")
    private String location;

    @Column(name = "max_capacity")
    private int max_capacity; 

    @JsonIgnore
    @OneToMany(targetEntity = Item.class, mappedBy = "warehouse")
    private Set<Item> items;

    public Warehouse() {
    }

    public Warehouse(String name, String location, int max_capacity, Set<Item> items) {
        this.name = name;
        this.location = location;
        this.max_capacity = max_capacity;
        this.items = items;
    }

    public Warehouse(int id, String name, String location, int max_capacity, Set<Item> items) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.max_capacity = max_capacity;
        this.items = items;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMax_capacity() {
        return max_capacity;
    }

    public void setMax_capacity(int max_capacity) {
        this.max_capacity = max_capacity;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + max_capacity;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
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
        Warehouse other = (Warehouse) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (max_capacity != other.max_capacity)
            return false;
        if (items == null) {
            if (other.items != null)
                return false;
        } else if (!items.equals(other.items))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Warehouse [id=" + id + ", name=" + name + ", location=" + location + ", max_capacity=" + max_capacity
                + ", items=" + items + "]";
    }

}
