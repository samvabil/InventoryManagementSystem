package com.skillstorm.inventory_management.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillstorm.inventory_management.DTO.ItemPatchRequest;
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

    /**
     * Retrieves all items 
     * @return list of all items
     */
    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Looks up a single item by its ID
     * @param id item ID
     * @return the matching Item, or null if not found
     */
    public Item findItemById(int id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.orElse(null);
    }

    /**
     * Retrieves all items stored in a specific warehouse 
     * @param warehouseId warehouse ID
     * @return list of items in the warehouse or an empty list if the warehouse does not exist
     */
    public List<Item> findItemsByWarehouseId(int warehouseId) {
        Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
        if (warehouse == null) {
            return Collections.emptyList();
        }
        return itemRepository.findByWarehouse(warehouse);
    }

    /**
     * Searches for items whose name contains the provided fragment, not case sensitive
     * @param nameFragment text fragment to search for
     * @return matching items
     */
    public List<Item> searchItemsByName(String nameFragment) {
        return itemRepository.findByNameContainingIgnoreCase(nameFragment);
    }

    /**
     * Searches for items whose SKU contains the provided fragment, not case sensitive
     * @param skuFragment text fragment to search for within the SKU
     * @return matching items
     */
    public List<Item> searchItemsBySku(String skuFragment) {
        return itemRepository.findBySkuContainingIgnoreCase(skuFragment);
    }

    /**
     * Creates a new item and assigns it to a specific warehouse
     *
     * Validation rules:
     * <ul>
     *     <li>Name is required and cannot be blank</li>
     *     <li>SKU is required and cannot be blank</li>
     *     <li>Quantity must be zero or positive</li>
     *     <li>Warehouse must have enough remaining capacity</li>
     * </ul>
     *
     * @param warehouseId target warehouse ID
     * @param item item data to create
     * @return the saved item
     *
     * @throws IllegalArgumentException if the warehouse is not found or
     *                                  basic item fields are invalid
     * @throws IllegalStateException if the warehouse lacks capacity
     */
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

    /**
     * Performs a full update of an existing item
     *
     * This method:
     * <ul>
     *     <li>Validates the new item fields</li>
     *     <li>Optionally moves the item to a different warehouse</li>
     *     <li>Ensures the target warehouse capacity is not exceeded
     *         after the change</li>
     * </ul>
     *
     * @param itemId ID of the item to update
     * @param updatedItem new item data
     * @return the updated item
     *
     * @throws IllegalArgumentException if the item or target warehouse
     *                                  does not exist or data is invalid
     * @throws IllegalStateException if the update would exceed warehouse capacity
     */
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

    /**
     * Applies a partial update to an item using a patch request
     *
     * Only non null fields in the patch request are applied to the
     * existing item. Capacity constraints are checked when quantity
     * changes.
     *
     * @param itemId ID of the item to update
     * @param patch patch object containing optional new values
     * @return the updated item
     *
     * @throws IllegalArgumentException if the item is not found or
     *                                  validation fails
     * @throws IllegalStateException if the updated quantity would exceed
     *                               warehouse capacity
     */
    @Transactional
    public Item patchItem(int itemId, ItemPatchRequest patch) {
        Item existing = findItemById(itemId);
        if (existing == null) {
            throw new IllegalArgumentException("Item with id " + itemId + " not found");
        }

        String newName = existing.getName();
        if (patch.getName() != null) {
            String trimmed = patch.getName().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Item name cannot be blank");
            }
            newName = trimmed;
        }

        String newSku = existing.getSku();
        if (patch.getSku() != null) {
            String trimmed = patch.getSku().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Item SKU cannot be blank");
            }
            newSku = trimmed;
        }

        int newQuantity = existing.getQuantity();
        if (patch.getQuantity() != null) {
            if (patch.getQuantity() < 0) {
                throw new IllegalArgumentException("Item quantity cannot be negative");
            }
            newQuantity = patch.getQuantity();
        }

        String newStorageLocation = existing.getStorageLocation();
        if (patch.getStorageLocation() != null) {
            newStorageLocation = patch.getStorageLocation();
        }

        Warehouse warehouse = existing.getWarehouse();
        if (warehouse != null && newQuantity != existing.getQuantity()) {
            int warehouseId = warehouse.getId();

            int currentLoad = warehouseService.getCurrentWarehouseLoad(warehouseId);
            int loadWithoutThisItem = currentLoad - existing.getQuantity();
            int projectedLoad = loadWithoutThisItem + newQuantity;

            if (projectedLoad > warehouse.getMax_capacity()) {
                throw new IllegalStateException("Updating item would exceed warehouse capacity");
            }
        }

        existing.setName(newName);
        existing.setSku(newSku);
        existing.setQuantity(newQuantity);
        existing.setStorageLocation(newStorageLocation);

        return itemRepository.save(existing);
    }

    /**
     * Deletes an item by ID, if it exists
     *
     * If the item is not found, no exception is thrown and the method
     * simply returns
     *
     * @param itemId ID of the item to delete
     */
    @Transactional
    public void deleteItemById(int itemId) {
        if (!itemRepository.existsById(itemId)) {
            return;
        }
        itemRepository.deleteById(itemId);
    }

    /**
     * Transfers a quantity of an item from one warehouse to another
     *
     * The method:
     * <ul>
     *     <li>Validates that the item exists</li>
     *     <li>Checks that the item is stored in the source warehouse</li>
     *     <li>Ensures the source has enough quantity</li>
     *     <li>Checks capacity of the destination warehouse</li>
     *     <li>Reduces quantity in the source warehouse</li>
     *     <li>Either updates an existing destination item with the same SKU
     *         or creates a new one</li>
     * </ul>
     *
     * @param itemId ID of the item to transfer
     * @param fromWarehouseId source warehouse ID
     * @param toWarehouseId destination warehouse ID
     * @param quantity quantity to transfer (must be positive)
     *
     * @throws IllegalArgumentException if the item or warehouses are not
     *                                  found or quantity is not positive
     * @throws IllegalStateException if the item is not stored in the source
     *                               warehouse, there is insufficient quantity,
     *                               or the destination lacks capacity
     */
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
                itemRepository.findBySkuIgnoreCase(item.getSku());

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

    /**
     * Validates core item fields such as name, SKU and quantity
     *
     * @param item item to validate
     *
     * @throws IllegalArgumentException if any required field is missing or invalid
     */
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

    /**
     * Returns the total quantity of a given SKU across all warehouses
     *
     * @param sku SKU string (required, cannot be blank)
     * @return total quantity for that SKU, or zero if none exist
     *
     * @throws IllegalArgumentException if the SKU is null or blank
     */
    public int getTotalQuantityBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        return itemRepository.getTotalQuantityBySku(sku.trim());
    }
}
