package com.skillstorm.inventory_management.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.skillstorm.inventory_management.Model.Item;
import com.skillstorm.inventory_management.Model.Warehouse;
import com.skillstorm.inventory_management.Repository.ItemRepository;
import com.skillstorm.inventory_management.Repository.WarehouseRepository;

/**
 * Service layer for managing Warehouse entities and related capacity logic.
 *
 * This class encapsulates operations for warehouse CRUD, aggregate load
 * calculation, and capacity checks used by higher level item operations.
 */
@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            ItemRepository itemRepository) {
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Retrieves all warehouses.
     *
     * @return list of all warehouses
     */
    public List<Warehouse> findAllWarehouses() {
        return warehouseRepository.findAll();
    }

    /**
     * Looks up a warehouse by its ID.
     *
     * @param id warehouse ID
     * @return the matching Warehouse, or null if not found
     */
    public Warehouse findWarehouseById(int id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
        return warehouse.orElse(null);
    }

    /**
     * Creates or updates a warehouse.
     *
     * If the warehouse has an ID that exists, it will be updated.
     * Otherwise a new record will be created.
     *
     * @param warehouse warehouse to save
     * @return persisted warehouse entity
     */
    public Warehouse saveWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    /**
     * Deletes a warehouse by ID.
     *
     * Note: This method does not perform checks for attached items.
     * Callers should ensure it is safe to delete, or let the database
     * enforce referential constraints.
     *
     * @param id warehouse ID to delete
     */
    public void deleteWarehouseById(int id) {
        warehouseRepository.deleteById(id);
    }

    /**
     * Computes the current load of a warehouse.
     *
     * The current load is defined as the sum of quantities of all
     * items assigned to this warehouse.
     *
     * If the warehouse does not exist, zero is returned.
     *
     * @param warehouseId warehouse ID
     * @return sum of item quantities in the warehouse
     */
    public int getCurrentWarehouseLoad(int warehouseId) {
        Warehouse warehouse = findWarehouseById(warehouseId);
        if (warehouse == null) {
            return 0;
        }

        List<Item> items = itemRepository.findByWarehouse(warehouse);

        return items.stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }

    /**
     * Calculates the remaining capacity for a warehouse.
     *
     * Remaining capacity is defined as:
     * <pre>
     * maxCapacity minus currentLoad
     * </pre>
     * and is never negative.
     *
     * If the warehouse is not found, zero is returned.
     *
     * @param warehouseId warehouse ID
     * @return remaining capacity (zero or positive)
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
     * Checks whether a warehouse has enough free capacity to store an
     * additional quantity.
     *
     * @param warehouseId warehouse ID
     * @param additionalQuantity quantity to be added
     * @return true if remaining capacity is greater than or equal to
     *         the additional quantity, false otherwise
     */
    public boolean hasCapacityFor(int warehouseId, int additionalQuantity) {
        int remaining = getRemainingCapacity(warehouseId);
        return additionalQuantity <= remaining;
    }
}
