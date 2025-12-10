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

    @GetMapping
    public ResponseEntity<List<Item>> findAllItems() {
        List<Item> items = itemService.findAllItems();
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findItemById(@PathVariable int id) {
        Item item = itemService.findItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Item>> findItemsByWarehouse(@PathVariable int warehouseId) {
        List<Item> items = itemService.findItemsByWarehouseId(warehouseId);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Item>> searchByName(@RequestParam String q) {
        return new ResponseEntity<>(itemService.searchItemsByName(q), HttpStatus.OK);
    }

    @GetMapping("/search/sku")
    public ResponseEntity<List<Item>> searchBySku(@RequestParam String q) {
        return new ResponseEntity<>(itemService.searchItemsBySku(q), HttpStatus.OK);
    }

    @PostMapping("/warehouse/{warehouseId}")
    public ResponseEntity<Item> createItem(
            @PathVariable int warehouseId,
            @RequestBody Item item) {

        Item newItem = itemService.addItemToWarehouse(warehouseId, item);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable int id,
            @RequestBody Item item) {

        Item updated = itemService.updateItem(id, item);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Item> patchItem(
            @PathVariable int id,
            @RequestBody ItemPatchRequest patchRequest) {

        Item updated = itemService.patchItem(id, patchRequest);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable int id) {
        itemService.deleteItemById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{itemId}/transfer")
    public ResponseEntity<Void> transferItem(
            @PathVariable int itemId,
            @RequestParam int fromWarehouse,
            @RequestParam int toWarehouse,
            @RequestParam int quantity) {

        itemService.transferItem(itemId, fromWarehouse, toWarehouse, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sku/{sku}/total")
    public ResponseEntity<Integer> getTotalQuantityBySku(@PathVariable String sku) {
        int total = itemService.getTotalQuantityBySku(sku);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }
}
