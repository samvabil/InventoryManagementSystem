package com.skillstorm.inventory_management.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillstorm.inventory_management.DTO.ItemPatchRequest;
import com.skillstorm.inventory_management.Model.Item;
import com.skillstorm.inventory_management.Service.ItemService;

@RestController
@RequestMapping("/items")
@CrossOrigin("*") // for development
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Retrieves all items 
     * @return list of items with HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Item>> findAllItems() {
        List<Item> items = itemService.findAllItems();
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    /**
     * Retrieves an item by its ID
     * @param id item ID
     * @return item with HTTP 200 or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> findItemById(@PathVariable int id) {
        Item item = itemService.findItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    /**
     * Retrieves all items within a warehouse
     * @param warehouseId warehouse ID 
     * @return list of items in warehouse with HTTP 200
     */
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Item>> findItemsByWarehouse(@PathVariable int warehouseId) {
        List<Item> items = itemService.findItemsByWarehouseId(warehouseId);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    /**
     * Searches items by name, not case sensitive
     * @param q text query for partial name matching
     * @return matching items with HTTP 200
     */
    @GetMapping("/search/name")
    public ResponseEntity<List<Item>> searchByName(@RequestParam String q) {
        return new ResponseEntity<>(itemService.searchItemsByName(q), HttpStatus.OK);
    }

    /**
     * Searches items by SKU, not case sensitive
     * @param q SKU query
     * @return matching items with HTTP 200
     */
    @GetMapping("/search/sku")
    public ResponseEntity<List<Item>> searchBySku(@RequestParam String q) {
        return new ResponseEntity<>(itemService.searchItemsBySku(q), HttpStatus.OK);
    }

    /**
     * Creates a new item in specified warehouse
     * @param warehouseId warehouse ID, which warehouse to assign item to
     * @param item request body with item fields
     * @return created item with HTTP 201
     */
    @PostMapping("/warehouse/{warehouseId}")
    public ResponseEntity<Item> createItem(
            @PathVariable int warehouseId,
            @RequestBody Item item) {

        Item newItem = itemService.addItemToWarehouse(warehouseId, item);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    /**
     * Fully updates an existing item by ID
     * @param id item ID
     * @param item request body with updated fields 
     * @return updated item with HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable int id,
            @RequestBody Item item) {

        Item updated = itemService.updateItem(id, item);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Partially updates an item with only fields provided in request body
     * @param id item ID of item to be updated
     * @param patchRequest object containing optional fields to update
     * @return updated item with HTTP 200
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Item> patchItem(
            @PathVariable int id,
            @RequestBody ItemPatchRequest patchRequest) {

        Item updated = itemService.patchItem(id, patchRequest);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Deletes an item by its ID
     * @param id item ID
     * @return HTTP 204 after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable int id) {
        itemService.deleteItemById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfer quantity of item from one warehouse to another
     * @param itemId item ID
     * @param fromWarehouse souce warehouse ID
     * @param toWarehouse destination warehouse ID
     * @param quantity number of units to be transferred
     * @return HTTP 200 on success 
     */
    @PostMapping("/{itemId}/transfer")
    public ResponseEntity<Void> transferItem(
            @PathVariable int itemId,
            @RequestParam int fromWarehouse,
            @RequestParam int toWarehouse,
            @RequestParam int quantity) {

        itemService.transferItem(itemId, fromWarehouse, toWarehouse, quantity);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieve total quantity of specific SKU across all warehouses
     * @param sku SKU string 
     * @return number of items with specified SKU in all warehouses with HTTP 200
     */
    @GetMapping("/sku/{sku}/total")
    public ResponseEntity<Integer> getTotalQuantityBySku(@PathVariable String sku) {
        int total = itemService.getTotalQuantityBySku(sku);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }
}
