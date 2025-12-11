/**
 * Extends JpaRepository, which provides CRUD operations and pagination
 */
package com.skillstorm.inventory_management.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillstorm.inventory_management.Model.Inventory;
import com.skillstorm.inventory_management.Model.Product;
import com.skillstorm.inventory_management.Model.Warehouse;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    List<Inventory> findByWarehouse(Warehouse warehouse);

    Optional<Inventory> findByWarehouseAndProduct(Warehouse warehouse, Product product);

    List<Inventory> findByWarehouseAndProduct_NameContainingIgnoreCase(
            Warehouse warehouse,
            String nameFragment
    );

    List<Inventory> findByWarehouseAndProduct_SkuContainingIgnoreCase(
            Warehouse warehouse,
            String skuFragment
    );

    List<Inventory> findByWarehouseAndProduct_CategoryContainingIgnoreCase(
            Warehouse warehouse,
            String categoryFragment
    );
}