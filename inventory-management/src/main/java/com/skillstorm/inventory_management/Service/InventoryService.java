package com.skillstorm.inventory_management.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Inventory findById(int id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    public List<Inventory> findInventoryByWarehouseId(int warehouseId) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        return inventoryRepository.findByWarehouse(warehouse);
    }

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

    public List<Inventory> searchByCategoryInWarehouse(int warehouseId, String categoryFragment) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        if (categoryFragment == null || categoryFragment.trim().isEmpty()) {
            return inventoryRepository.findByWarehouse(warehouse);
        }
        return inventoryRepository.findByWarehouseAndProduct_CategoryContainingIgnoreCase(
                warehouse,
                categoryFragment.trim()
        );
    }

    @Transactional
    public Inventory addInventoryToWarehouse(int warehouseId,
                                             String sku,
                                             String name,
                                             String description,
                                             String category,
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

    @Transactional
    public void deleteInventoryById(int inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            return;
        }
        inventoryRepository.deleteById(inventoryId);
    }

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
