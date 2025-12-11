/**
 * Extends JpaRepository, which provides CRUD operations and pagination
 */
package com.skillstorm.inventory_management.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillstorm.inventory_management.Model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{
    Optional<Product> findBySkuIgnoreCase(String sku);
}
