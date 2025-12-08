package com.skillstorm.inventory_management.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillstorm.inventory_management.Model.Item;
import com.skillstorm.inventory_management.Model.Warehouse;


public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByWarehouse(Warehouse warehouse);
    List<Item> findByNameContainingIgnoreCase(String name);
    List<Item> findBySKUContainingIgnoreCase(String sku);
}
