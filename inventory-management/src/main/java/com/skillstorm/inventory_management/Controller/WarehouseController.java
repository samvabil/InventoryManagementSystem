package com.skillstorm.inventory_management.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillstorm.inventory_management.Model.Warehouse;
import com.skillstorm.inventory_management.Service.WarehouseService;

@RestController
@RequestMapping("/warehouses")
@CrossOrigin("*") // for development
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public ResponseEntity<List<Warehouse>> findAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.findAllWarehouses();
        return new ResponseEntity<>(warehouses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> findWarehouseById(@PathVariable int id) {
        Warehouse warehouse = warehouseService.findWarehouseById(id);
        if (warehouse == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(warehouse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        Warehouse newWarehouse = warehouseService.saveWarehouse(warehouse);
        return new ResponseEntity<>(newWarehouse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(
            @PathVariable int id,
            @RequestBody Warehouse warehouse) {

        Warehouse existing = warehouseService.findWarehouseById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        warehouse.setId(id);
        Warehouse updated = warehouseService.saveWarehouse(warehouse);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable int id) {
        warehouseService.deleteWarehouseById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/capacity")
    public ResponseEntity<Integer> getRemainingCapacity(@PathVariable int id) {
        int remaining = warehouseService.getRemainingCapacity(id);
        return new ResponseEntity<>(remaining, HttpStatus.OK);
    }

    @GetMapping("/{id}/current-load")
    public ResponseEntity<Integer> getCurrentWarehouseLoad(@PathVariable int id) {
        int load = warehouseService.getCurrentWarehouseLoad(id);
        return new ResponseEntity<>(load, HttpStatus.OK);
    }
}
