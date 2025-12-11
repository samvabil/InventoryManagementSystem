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

    /**
     * Retrieves all warehouses 
     * @return list of warehouses with HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Warehouse>> findAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.findAllWarehouses();
        return new ResponseEntity<>(warehouses, HttpStatus.OK);
    }

    /**
     * Retrieves a warehouse by its ID
     * @param id warehouse ID
     * @return warehouse with HTTP 200 or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> findWarehouseById(@PathVariable int id) {
        Warehouse warehouse = warehouseService.findWarehouseById(id);
        if (warehouse == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(warehouse, HttpStatus.OK);
    }

    /**
     * Creates a new warehouse
     * @param warehouse request body with warehouse fields
     * @return created warehouse with HTTP 201
     */
    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        Warehouse newWarehouse = warehouseService.saveWarehouse(warehouse);
        return new ResponseEntity<>(newWarehouse, HttpStatus.CREATED);
    }

    /**
     * Updates an existing warehouse by its ID
     * @param id warehouse ID
     * @param warehouse updated warehouse fields
     * @returnupdated warehouse with HTTP 200 or HTTP 404 if not found
     */
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

    /**
     * Deletes a warehouse by its ID
     * @param id warehouse ID
     * @return HTTP 204 after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable int id) {
        warehouseService.deleteWarehouseById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves remaining capacity of warehouse
     * @param id warehouse ID
     * @return remaining capacity with HTTP 200
     */
    @GetMapping("/{id}/capacity")
    public ResponseEntity<Integer> getRemainingCapacity(@PathVariable int id) {
        int remaining = warehouseService.getRemainingCapacity(id);
        return new ResponseEntity<>(remaining, HttpStatus.OK);
    }

    /**
     * Retrieves current load or total stored quality of warehouse
     * @param id warehouse ID
     * @return current load with HTTP 200
     */
    @GetMapping("/{id}/current-load")
    public ResponseEntity<Integer> getCurrentWarehouseLoad(@PathVariable int id) {
        int load = warehouseService.getCurrentWarehouseLoad(id);
        return new ResponseEntity<>(load, HttpStatus.OK);
    }
}
