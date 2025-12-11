package com.skillstorm.inventory_management.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.skillstorm.inventory_management.Model.Inventory;
import com.skillstorm.inventory_management.Model.Warehouse;
import com.skillstorm.inventory_management.Repository.InventoryRepository;
import com.skillstorm.inventory_management.Repository.WarehouseRepository;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            InventoryRepository inventoryRepository) {
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Returns all warehouses 
     * @return list of all warehouse entities
     */
    public List<Warehouse> findAllWarehouses() {
        return warehouseRepository.findAll();
    }

    /**
     * Finds a warehouse by its id
     * @param id warehouse id
     * @return Warehouse if found or null if it doesn't exist
     */
    public Warehouse findWarehouseById(int id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
        return warehouse.orElse(null);
    }

    /**
     * Creates or updates a warehouse
     * @param warehouse warehouse object to save
     * @return warehouse entity
     */
    public Warehouse saveWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    /**
     * Deletes a warehouse by id
     * @param id warehouse id to delete
     */
    public void deleteWarehouseById(int id) {
        warehouseRepository.deleteById(id);
    }

    /**
     * Calculates the total quantity of all inventory entries stored in a warehouse.
     * @param warehouseId warehouse id
     * @return sum of quantities for all Inventory rows in the warehouse or 0 if the warehouse does not exist
     */
    public int getCurrentWarehouseLoad(int warehouseId) {
        Warehouse warehouse = findWarehouseById(warehouseId);
        if (warehouse == null) {
            return 0;
        }

        List<Inventory> inventoryEntries = inventoryRepository.findByWarehouse(warehouse);

        return inventoryEntries.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
    }

    /**
     * Calculates remaining capacity for a warehouse based on its maximum capacity
     * and current load.
     * @param warehouseId id of the warehouse
     * @return remaining capacity which is always greater than or equal to zero, returns 0 if the warehouse does not exist.
     */
    public int getRemainingCapacity(int warehouseId) {
        Warehouse warehouse = findWarehouseById(warehouseId);
        if (warehouse == null) {
            return 0;
        }

        int currentLoad = getCurrentWarehouseLoad(warehouseId);
        int maxCapacity = warehouse.getMax_capacity();

        return Math.max(0, maxCapacity - currentLoad);
    }

    /**
     * Determines whether a warehouse has enough remaining capacity for the
     * specified additional quantity.
     * @param warehouseId       id of the warehouse
     * @param additionalQuantity quantity to be added 
     * @return true if the warehouse has enough remaining capacity, false otherwise 
     * @throws IllegalArgumentException if additionalQuantity is negative
     */
    public boolean hasCapacityFor(int warehouseId, int additionalQuantity) {
        if (additionalQuantity < 0) {
            throw new IllegalArgumentException("additionalQuantity cannot be negative");
        }
        int remaining = getRemainingCapacity(warehouseId);
        return additionalQuantity <= remaining;
    }
}
