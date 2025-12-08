package com.skillstorm.inventory_management.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillstorm.inventory_management.Model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer>{

}
