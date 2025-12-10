package com.skillstorm.inventory_management.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillstorm.inventory_management.Model.Item;
import com.skillstorm.inventory_management.Model.Warehouse;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByWarehouse(Warehouse warehouse);
    List<Item> findByNameContainingIgnoreCase(String name);
    List<Item> findBySkuContainingIgnoreCase(String sku);

    List<Item> findBySkuIgnoreCase(String sku);

    @Query("select coalesce(sum(i.quantity), 0) from Item i where lower(i.sku) = lower(:sku)")
    int getTotalQuantityBySku(@Param("sku") String sku);
}
