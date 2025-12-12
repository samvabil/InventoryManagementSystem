package com.skillstorm.inventory_management.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillstorm.inventory_management.Model.Department;
import com.skillstorm.inventory_management.Model.Inventory;
import com.skillstorm.inventory_management.Model.Product;
import com.skillstorm.inventory_management.Model.Warehouse;
import com.skillstorm.inventory_management.Repository.InventoryRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public InventoryService(InventoryRepository inventoryRepository,
                            WarehouseService warehouseService,
                            ProductService productService) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    /**
     * Find row in inventory by id 
     * @param id inventory row id
     * @return the inventory if found or null if does not exist
     */
    public Inventory findById(int id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    /**
     * Retrieve all inventory rows in warehouse
     * @param warehouseId warehouse id
     * @return list of Inventory entries for that warehouse or an empty list if the warehouse is not found
     */
    public List<Inventory> findInventoryByWarehouseId(int warehouseId) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        return inventoryRepository.findByWarehouse(warehouse);
    }

    /**
     * "Search" inventory in warehouse by product name 
     * @param warehouseId  warehouse id
     * @param nameFragment user search
     * @return matching entries or empty list if warehouse not found 
     */
    public List<Inventory> searchByProductNameInWarehouse(int warehouseId, String nameFragment) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        if (nameFragment == null || nameFragment.trim().isEmpty()) {
            return inventoryRepository.findByWarehouse(warehouse);
        }
        return inventoryRepository.findByWarehouseAndProduct_NameContainingIgnoreCase(
                warehouse,
                nameFragment.trim()
        );
    }

    /**
     * "Search" inventory in warehouse by SKU
     * @param warehouseId warehouse id
     * @param skuFragment user search
     * @return matching entries or empty list if warehouse not found
     */
    public List<Inventory> searchBySkuInWarehouse(int warehouseId, String skuFragment) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        if (skuFragment == null || skuFragment.trim().isEmpty()) {
            return inventoryRepository.findByWarehouse(warehouse);
        }
        return inventoryRepository.findByWarehouseAndProduct_SkuContainingIgnoreCase(
                warehouse,
                skuFragment.trim()
        );
    }

    /**
     * Add inventory to warehouse for given product
     * Create if it does not already exist 
     * Rules: warehouse has to exist, quantity can't be less than 0, warehouse must have remaining capacity for new quantity, 
     * if inventory row in this warehouse exists for product, quantity increased 
     * @param warehouseId     warehouse id 
     * @param sku             product SKU
     * @param name            product name 
     * @param description     product description
     * @param category        product category/department 
     * @param quantity        quantity to add 
     * @param storageLocation where located within warehouse 
     * @return the created or updated Inventory entity
     * @throws IllegalArgumentException if the warehouse does not exist or quantity is negative
     * @throws IllegalStateException    if the warehouse does not have enough capacity
     */
    @Transactional
    public Inventory addInventoryToWarehouse(int warehouseId,
                                             String sku,
                                             String name,
                                             String description,
                                             Department category,
                                             int quantity,
                                             String storageLocation) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Warehouse with id " + warehouseId + " not found");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (!warehouseService.hasCapacityFor(warehouseId, quantity)) {
            throw new IllegalStateException("Warehouse capacity exceeded for warehouse id " + warehouseId);
        }

        Product product = productService.findOrCreateProductBySku(
                sku,
                name,
                description,
                category
        );

        Optional<Inventory> existingOpt =
                inventoryRepository.findByWarehouseAndProduct(warehouse, product);

        Inventory inventory;
        if (existingOpt.isPresent()) {
            inventory = existingOpt.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
            if (storageLocation != null) {
                inventory.setStorageLocation(storageLocation);
            }
        } else {
            inventory = new Inventory();
            inventory.setWarehouse(warehouse);
            inventory.setProduct(product);
            inventory.setQuantity(quantity);
            inventory.setStorageLocation(storageLocation);
        }

        return inventoryRepository.save(inventory);
    }

    /**
     * Update quantity and storage location for existing inventory row
     * @param inventoryId        inventory row id 
     * @param newQuantity        new quantity 
     * @param newStorageLocation new storage location
     * @return the updated Inventory entity
     * @throws IllegalArgumentException if the inventory row is not found or quantity is negative
     * @throws IllegalStateException    if the update would exceed warehouse capacity
     */
    @Transactional
    public Inventory updateInventory(int inventoryId,
                                     int newQuantity,
                                     String newStorageLocation) {

        Inventory existing = findById(inventoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Inventory with id " + inventoryId + " not found");
        }

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Warehouse warehouse = existing.getWarehouse();
        int warehouseId = warehouse.getId();

        int currentLoad = warehouseService.getCurrentWarehouseLoad(warehouseId);
        int loadWithoutThisRow = currentLoad - existing.getQuantity();
        int projectedLoad = loadWithoutThisRow + newQuantity;

        if (projectedLoad > warehouse.getMax_capacity()) {
            throw new IllegalStateException("Updating quantity would exceed warehouse capacity");
        }

        existing.setQuantity(newQuantity);
        existing.setStorageLocation(newStorageLocation);

        return inventoryRepository.save(existing);
    }

    /**
     * Delete inventory row if it exists
     * @param inventoryId inventory row id 
     */
    @Transactional
    public void deleteInventoryById(int inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            return;
        }
        inventoryRepository.deleteById(inventoryId);
    }

    /**
     * Transfer quantity of product from one warehouse to another 
     * Rules: transfer quantity positive, source inventory exists and belongs to source warehouse,
     * source inventory row has enough quantity to transfer, destination warehouse exists and has capacity,
     * if inventory row exists in destination warehouse, quantity increased 
     * @param sourceInventoryId id of the inventory row in the source warehouse
     * @param fromWarehouseId   id of the source warehouse
     * @param toWarehouseId     id of the destination warehouse
     * @param quantityToTransfer quantity to transfer 
     * @throws IllegalArgumentException if warehouses or inventory row are not found,
     *                                  or transfer quantity is not positive
     * @throws IllegalStateException    if the inventory is not in the source warehouse,
     *                                  there is insufficient quantity, or the destination
     *                                  warehouse does not have enough capacity
     */
    @Transactional
    public void transferInventory(int sourceInventoryId,
                                  int fromWarehouseId,
                                  int toWarehouseId,
                                  int quantityToTransfer) {

        if (quantityToTransfer <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be positive");
        }

        Inventory source = findById(sourceInventoryId);
        if (source == null) {
            throw new IllegalArgumentException("Inventory row with id " + sourceInventoryId + " not found");
        }

        Warehouse fromWarehouse = warehouseService.findWarehouseById(fromWarehouseId);
        Warehouse toWarehouse = warehouseService.findWarehouseById(toWarehouseId);

        if (fromWarehouse == null || toWarehouse == null) {
            throw new IllegalArgumentException("Source or destination warehouse not found");
        }

        if (source.getWarehouse() == null || source.getWarehouse().getId() != fromWarehouseId) {
            throw new IllegalStateException("Inventory row is not in the source warehouse");
        }

        if (source.getQuantity() < quantityToTransfer) {
            throw new IllegalStateException("Not enough quantity to transfer");
        }

        if (!warehouseService.hasCapacityFor(toWarehouseId, quantityToTransfer)) {
            throw new IllegalStateException("Destination warehouse does not have enough capacity");
        }

        source.setQuantity(source.getQuantity() - quantityToTransfer);
        inventoryRepository.save(source);

        Product product = source.getProduct();
        Optional<Inventory> destinationOpt =
                inventoryRepository.findByWarehouseAndProduct(toWarehouse, product);

        Inventory destination;
        if (destinationOpt.isPresent()) {
            destination = destinationOpt.get();
            destination.setQuantity(destination.getQuantity() + quantityToTransfer);
        } else {
            destination = new Inventory();
            destination.setWarehouse(toWarehouse);
            destination.setProduct(product);
            destination.setQuantity(quantityToTransfer);
            destination.setStorageLocation(source.getStorageLocation());
        }

        inventoryRepository.save(destination);
    }
}
