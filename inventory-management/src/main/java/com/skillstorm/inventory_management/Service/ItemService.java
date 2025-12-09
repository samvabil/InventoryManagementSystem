package com.skillstorm.inventory_management.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillstorm.inventory_management.Model.Item;
import com.skillstorm.inventory_management.Model.Warehouse;
import com.skillstorm.inventory_management.Repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final WarehouseService warehouseService;

    public ItemService(ItemRepository itemRepository,
                       WarehouseService warehouseService) {
        this.itemRepository = itemRepository;
        this.warehouseService = warehouseService;
    }

    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    public Item findItemById(int id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.orElse(null);
    }

    public List<Item> findItemsByWarehouseId(int warehouseId) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        return itemRepository.findByWarehouse(warehouse);
    }

    public List<Item> searchItemsByName(String nameFragment) {
        return itemRepository.findByNameContainingIgnoreCase(nameFragment);
    }

    public List<Item> searchItemsBySku(String skuFragment) {
        return itemRepository.findBySkuContainingIgnoreCase(skuFragment);
    }

    @Transactional
    public Item addItemToWarehouse(int warehouseId, Item item) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Warehouse with id " + warehouseId + " not found");
        }

        validateItemBasicFields(item);

        boolean hasCapacity = warehouseService.hasCapacityFor(warehouseId, item.getQuantity());
        if (!hasCapacity) {
            throw new IllegalStateException("Warehouse capacity exceeded for warehouse id " + warehouseId);
        }

        item.setWarehouse(warehouse);
        return itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(int itemId, Item updatedItem) {
        Item existing = findItemById(itemId);
        if (existing == null) {
            throw new IllegalArgumentException("Item with id " + itemId + " not found");
        }

        validateItemBasicFields(updatedItem);

        Warehouse targetWarehouse = existing.getWarehouse();
        if (updatedItem.getWarehouse() != null) {
            Warehouse maybeNewWarehouse =
                    warehouseService.findWarehouseById(updatedItem.getWarehouse().getId());
            if (maybeNewWarehouse == null) {
                throw new IllegalArgumentException("Target warehouse not found");
            }
            targetWarehouse = maybeNewWarehouse;
        }

        int warehouseId = targetWarehouse.getId();

        int currentLoad = warehouseService.getCurrentWarehouseLoad(warehouseId);
        int loadWithoutThisItem = currentLoad - existing.getQuantity();

        int projectedLoad = loadWithoutThisItem + updatedItem.getQuantity();

        if (projectedLoad > targetWarehouse.getMax_capacity()) {
            throw new IllegalStateException("Updating item would exceed warehouse capacity");
        }

        existing.setName(updatedItem.getName());
        existing.setSku(updatedItem.getSku());
        existing.setQuantity(updatedItem.getQuantity());
        existing.setStorageLocation(updatedItem.getStorageLocation());
        existing.setWarehouse(targetWarehouse);

        return itemRepository.save(existing);
    }

    @Transactional
    public void deleteItemById(int itemId) {
        if (!itemRepository.existsById(itemId)) {
            return; 
        }
        itemRepository.deleteById(itemId);
    }

    @Transactional
    public void transferItem(int itemId, int fromWarehouseId, int toWarehouseId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be positive");
        }

        Item item = findItemById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item with id " + itemId + " not found");
        }

        Warehouse fromWarehouse = warehouseService.findWarehouseById(fromWarehouseId);
        Warehouse toWarehouse = warehouseService.findWarehouseById(toWarehouseId);

        if (fromWarehouse == null || toWarehouse == null) {
            throw new IllegalArgumentException("Source or destination warehouse not found");
        }

        if (item.getWarehouse() == null || item.getWarehouse().getId() != fromWarehouseId) {
            throw new IllegalStateException("Item is not stored in the source warehouse");
        }

        if (item.getQuantity() < quantity) {
            throw new IllegalStateException("Not enough quantity to transfer");
        }

        boolean destinationHasCapacity =
                warehouseService.hasCapacityFor(toWarehouseId, quantity);
        if (!destinationHasCapacity) {
            throw new IllegalStateException("Destination warehouse does not have enough capacity");
        }

        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        List<Item> destinationItemsWithSameSku =
                itemRepository.findBySkuContainingIgnoreCase(item.getSku());

        Item destinationItem = destinationItemsWithSameSku.stream()
                .filter(i -> i.getWarehouse() != null && i.getWarehouse().getId() == toWarehouseId)
                .findFirst()
                .orElse(null);

        if (destinationItem == null) {
            Item newItem = new Item(
                    item.getName(),
                    item.getSku(),
                    quantity,
                    item.getStorageLocation(),
                    toWarehouse
            );
            itemRepository.save(newItem);
        } else {
            destinationItem.setQuantity(destinationItem.getQuantity() + quantity);
            itemRepository.save(destinationItem);
        }
    }

    private void validateItemBasicFields(Item item) {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (item.getSku() == null || item.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Item SKU is required");
        }
        if (item.getQuantity() < 0) {
            throw new IllegalArgumentException("Item quantity cannot be negative");
        }
    }
}
