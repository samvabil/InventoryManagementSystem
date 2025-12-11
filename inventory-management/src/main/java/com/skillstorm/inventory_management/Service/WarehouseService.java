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

    public List<Warehouse> findAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Warehouse findWarehouseById(int id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
        return warehouse.orElse(null);
    }

    public Warehouse saveWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public void deleteWarehouseById(int id) {
        warehouseRepository.deleteById(id);
    }

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

    public int getRemainingCapacity(int warehouseId) {
        Warehouse warehouse = findWarehouseById(warehouseId);
        if (warehouse == null) {
            return 0;
        }

        int currentLoad = getCurrentWarehouseLoad(warehouseId);
        int maxCapacity = warehouse.getMax_capacity();

        return Math.max(0, maxCapacity - currentLoad);
    }

    public boolean hasCapacityFor(int warehouseId, int additionalQuantity) {
        if (additionalQuantity < 0) {
            throw new IllegalArgumentException("additionalQuantity cannot be negative");
        }
        int remaining = getRemainingCapacity(warehouseId);
        return additionalQuantity <= remaining;
    }
}
