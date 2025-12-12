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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillstorm.inventory_management.DTO.InventoryCreateRequest;
import com.skillstorm.inventory_management.DTO.InventoryUpdateRequest;
import com.skillstorm.inventory_management.Model.Inventory;
import com.skillstorm.inventory_management.Service.InventoryService;

@RestController
@RequestMapping("/inventory") 
@CrossOrigin("*") // for development
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Retrieves an inventory row by id
     * @param id inventory id
     * @return inventory row with HTTP 200 or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> findInventoryById(@PathVariable int id) {
        Inventory inventory = inventoryService.findById(id);
        if (inventory == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(inventory, HttpStatus.OK);
    }

    /**
     * Retrieves all inventory rows within a warehouse
     * @param warehouseId warehouse id
     * @return list of inventory entries with HTTP 200
     */
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Inventory>> findInventoryByWarehouse(@PathVariable int warehouseId) {
        List<Inventory> entries = inventoryService.findInventoryByWarehouseId(warehouseId);
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    /**
     * Searches inventory in a warehouse by product name fragment, case insensitive
     * @param warehouseId warehouse id
     * @param q           text query for partial name matching
     * @return matching inventory entries with HTTP 200
     */
    @GetMapping("/warehouse/{warehouseId}/search/name")
    public ResponseEntity<List<Inventory>> searchByName(
            @PathVariable int warehouseId,
            @RequestParam("q") String q) {

        List<Inventory> entries = inventoryService.searchByProductNameInWarehouse(warehouseId, q);
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    /**
     * Searches inventory in a warehouse by SKU fragment, case insensitive
     * @param warehouseId warehouse id
     * @param q           SKU fragment
     * @return matching inventory entries with HTTP 200
     */
    @GetMapping("/warehouse/{warehouseId}/search/sku")
    public ResponseEntity<List<Inventory>> searchBySku(
            @PathVariable int warehouseId,
            @RequestParam("q") String q) {

        List<Inventory> entries = inventoryService.searchBySkuInWarehouse(warehouseId, q);
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    /**
     * Creates a new inventory row in the specified warehouse
     * @param warehouseId warehouse id
     * @param request     request body with product and inventory fields
     * @return created inventory row with HTTP 201
     */
    @PostMapping("/warehouse/{warehouseId}")
    public ResponseEntity<Inventory> createInventory(
            @PathVariable int warehouseId,
            @RequestBody InventoryCreateRequest request) {

        Inventory created = inventoryService.addInventoryToWarehouse(
                warehouseId,
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getQuantity(),
                request.getStorageLocation()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Fully updates quantity and storage location for an inventory row
     * @param id      inventory id
     * @param request request body with quantity and storage location
     * @return updated inventory with HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable int id,
            @RequestBody InventoryUpdateRequest request) {

        Inventory updated = inventoryService.updateInventory(
                id,
                request.getQuantity(),
                request.getStorageLocation()
        );
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Deletes an inventory row by id
     * @param id inventory id
     * @return HTTP 204 after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable int id) {
        inventoryService.deleteInventoryById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfers quantity of an inventory row from one warehouse to another
     * @param inventoryId   inventory row id
     * @param fromWarehouse source warehouse id
     * @param toWarehouse   destination warehouse id
     * @param quantity      quantity to transfer
     * @return HTTP 200 on success
     */
    @PostMapping("/{inventoryId}/transfer")
    public ResponseEntity<Void> transferInventory(
            @PathVariable int inventoryId,
            @RequestParam int fromWarehouse,
            @RequestParam int toWarehouse,
            @RequestParam int quantity) {

        inventoryService.transferInventory(inventoryId, fromWarehouse, toWarehouse, quantity);
        return ResponseEntity.ok().build();
    }
}
