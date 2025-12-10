const API_BASE_URL = "http://localhost:8080";

const warehouseCardsContainer = document.getElementById("warehouse-cards");
const addWarehouseBtn = document.getElementById("add-warehouse-btn");

const warehouseModal = document.getElementById("warehouse-modal");
const warehouseModalTitle = document.getElementById("warehouse-modal-title");
const warehouseForm = document.getElementById("warehouse-form");
const warehouseIdInput = document.getElementById("warehouse-id");
const warehouseNameInput = document.getElementById("warehouse-name");
const warehouseLocationInput = document.getElementById("warehouse-location");
const warehouseMaxCapacityInput = document.getElementById("warehouse-max-capacity");
const warehouseCancelBtn = document.getElementById("warehouse-cancel-btn");

const itemsModal = document.getElementById("items-modal");
const itemsModalTitle = document.getElementById("items-modal-title");
const itemsListContainer = document.getElementById("items-list");
const itemForm = document.getElementById("item-form");
const itemIdInput = document.getElementById("item-id");
const itemWarehouseIdInput = document.getElementById("item-warehouse-id");
const itemNameInput = document.getElementById("item-name");
const itemSkuInput = document.getElementById("item-sku");
const itemQuantityInput = document.getElementById("item-quantity");
const itemStorageLocationInput = document.getElementById("item-storage-location");
const itemCancelBtn = document.getElementById("item-cancel-btn");

const overlay = document.getElementById("overlay");

function openModal(modal) {
    modal.classList.remove("hidden");
    overlay.classList.remove("hidden");
}

function closeModal(modal) {
    modal.classList.add("hidden");
    overlay.classList.add("hidden");
}

async function apiGet(path) {
    const res = await fetch(`${API_BASE_URL}${path}`);
    if (!res.ok) {
        throw new Error(`GET ${path} failed with status ${res.status}`);
    }
    return res.json();
}

async function apiPost(path, body) {
    const res = await fetch(`${API_BASE_URL}${path}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });
    if (!res.ok) {
        throw new Error(`POST ${path} failed with status ${res.status}`);
    }
    return res.json();
}

async function apiPut(path, body) {
    const res = await fetch(`${API_BASE_URL}${path}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });
    if (!res.ok) {
        throw new Error(`PUT ${path} failed with status ${res.status}`);
    }
    return res.json();
}

async function apiDelete(path) {
    const res = await fetch(`${API_BASE_URL}${path}`, { method: "DELETE" });
    if (!res.ok && res.status !== 204) {
        throw new Error(`DELETE ${path} failed with status ${res.status}`);
    }
}

async function loadWarehouses() {
    warehouseCardsContainer.innerHTML = "<p>Loading warehouses...</p>";

    try {
        const warehouses = await apiGet("/warehouses");
        if (!warehouses.length) {
            warehouseCardsContainer.innerHTML = "<p>No warehouses yet. Click 'Add Warehouse' to create one.</p>";
            return;
        }

        warehouseCardsContainer.innerHTML = "";
        for (const warehouse of warehouses) {
            const card = await createWarehouseCard(warehouse);
            warehouseCardsContainer.appendChild(card);
        }
    } catch (err) {
        console.error(err);
        warehouseCardsContainer.innerHTML = "<p>Failed to load warehouses.</p>";
    }
}

async function createWarehouseCard(warehouse) {
    const card = document.createElement("div");
    card.className = "card";

    let currentLoad = 0;
    let remainingCapacity = 0;

    try {
        const [load, remaining] = await Promise.all([
            apiGet(`/warehouses/${warehouse.id}/current-load`),
            apiGet(`/warehouses/${warehouse.id}/capacity`)
        ]);
        currentLoad = load;
        remainingCapacity = remaining;
    } catch (err) {
        console.error("Failed to fetch metrics for warehouse", warehouse.id, err);
    }

    const header = document.createElement("div");
    header.className = "card-header";

    const title = document.createElement("h3");
    title.textContent = warehouse.name;

    const capacityBadge = document.createElement("span");
    capacityBadge.style.fontSize = "0.8rem";
    capacityBadge.style.color = "#6b7280";
    capacityBadge.textContent = `ID: ${warehouse.id}`;

    header.appendChild(title);
    header.appendChild(capacityBadge);

    const subtitle = document.createElement("div");
    subtitle.className = "card-subtitle";
    subtitle.textContent = warehouse.location;

    const metrics = document.createElement("div");
    metrics.className = "card-metrics";
    metrics.innerHTML = `
        <p><strong>Max capacity:</strong> ${warehouse.max_capacity}</p>
        <p><strong>Current load:</strong> ${currentLoad}</p>
        <p><strong>Remaining:</strong> ${remainingCapacity}</p>
    `;

    const actions = document.createElement("div");
    actions.className = "card-actions";

    const viewItemsBtn = document.createElement("button");
    viewItemsBtn.className = "btn secondary";
    viewItemsBtn.textContent = "View items";
    viewItemsBtn.addEventListener("click", () => openItemsModal(warehouse));

    const addItemBtn = document.createElement("button");
    addItemBtn.className = "btn secondary";
    addItemBtn.textContent = "Add item";
    addItemBtn.addEventListener("click", () => openItemsModal(warehouse, true));

    const editBtn = document.createElement("button");
    editBtn.className = "btn secondary";
    editBtn.textContent = "Edit";
    editBtn.addEventListener("click", () => openWarehouseModal(warehouse));

    const deleteBtn = document.createElement("button");
    deleteBtn.className = "btn danger";
    deleteBtn.textContent = "Delete";
    deleteBtn.addEventListener("click", async () => {
        const confirmed = window.confirm(`Delete warehouse "${warehouse.name}"? This may fail if it still has items.`);
        if (!confirmed) return;

        try {
            await apiDelete(`/warehouses/${warehouse.id}`);
            await loadWarehouses();
        } catch (err) {
            console.error(err);
            alert("Failed to delete warehouse. Check backend logs, it may have related items.");
        }
    });

    actions.appendChild(viewItemsBtn);
    actions.appendChild(addItemBtn);
    actions.appendChild(editBtn);
    actions.appendChild(deleteBtn);

    card.appendChild(header);
    card.appendChild(subtitle);
    card.appendChild(metrics);
    card.appendChild(actions);

    return card;
}

function openWarehouseModal(warehouse) {
    if (warehouse) {
        warehouseModalTitle.textContent = "Edit Warehouse";
        warehouseIdInput.value = warehouse.id;
        warehouseNameInput.value = warehouse.name;
        warehouseLocationInput.value = warehouse.location;
        warehouseMaxCapacityInput.value = warehouse.max_capacity;
    } else {
        warehouseModalTitle.textContent = "Add Warehouse";
        warehouseIdInput.value = "";
        warehouseNameInput.value = "";
        warehouseLocationInput.value = "";
        warehouseMaxCapacityInput.value = "";
    }

    openModal(warehouseModal);
}

warehouseForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = warehouseIdInput.value;
    const payload = {
        name: warehouseNameInput.value.trim(),
        location: warehouseLocationInput.value.trim(),
        max_capacity: Number(warehouseMaxCapacityInput.value)
    };

    try {
        if (id) {
            const updated = await apiPut(`/warehouses/${id}`, {
                id: Number(id),
                ...payload
            });
            console.log("Updated warehouse", updated);
        } else {
            const created = await apiPost("/warehouses", payload);
            console.log("Created warehouse", created);
        }

        closeModal(warehouseModal);
        await loadWarehouses();
    } catch (err) {
        console.error(err);
        alert("Failed to save warehouse");
    }
});

warehouseCancelBtn.addEventListener("click", () => closeModal(warehouseModal));
overlay.addEventListener("click", () => {
    closeModal(warehouseModal);
    closeModal(itemsModal);
});

async function openItemsModal(warehouse, startWithAddForm = false) {
    itemsModalTitle.textContent = `Items in ${warehouse.name}`;
    itemWarehouseIdInput.value = warehouse.id;
    itemIdInput.value = "";
    if (!startWithAddForm) {
        itemNameInput.value = "";
        itemSkuInput.value = "";
        itemQuantityInput.value = "";
        itemStorageLocationInput.value = "";
    }

    await loadItemsForWarehouse(warehouse.id);

    openModal(itemsModal);
}

async function loadItemsForWarehouse(warehouseId) {
    itemsListContainer.innerHTML = "<p>Loading items...</p>";

    try {
        const items = await apiGet(`/items/warehouse/${warehouseId}`);

        if (!items.length) {
            itemsListContainer.innerHTML = "<p>No items in this warehouse yet.</p>";
            return;
        }

        itemsListContainer.innerHTML = "";

        items.forEach(item => {
            const row = document.createElement("div");
            row.className = "item-row";

            const main = document.createElement("div");
            main.className = "item-row-main";

            const line1 = document.createElement("span");
            line1.textContent = `${item.name} (SKU: ${item.sku})`;

            const line2 = document.createElement("span");
            line2.textContent = `Qty: ${item.quantity} | Location: ${item.storageLocation}`;

            main.appendChild(line1);
            main.appendChild(line2);

            const actions = document.createElement("div");
            actions.className = "item-row-actions";

            const editBtn = document.createElement("button");
            editBtn.className = "btn secondary";
            editBtn.textContent = "Edit";
            editBtn.addEventListener("click", () => fillItemFormForEdit(item));

            const deleteBtn = document.createElement("button");
            deleteBtn.className = "btn danger";
            deleteBtn.textContent = "Delete";
            deleteBtn.addEventListener("click", async () => {
                const confirmed = window.confirm(`Delete item "${item.name}"?`);
                if (!confirmed) return;

                try {
                    await apiDelete(`/items/${item.id}`);
                    await loadItemsForWarehouse(warehouseId);
                } catch (err) {
                    console.error(err);
                    alert("Failed to delete item");
                }
            });

            actions.appendChild(editBtn);
            actions.appendChild(deleteBtn);

            row.appendChild(main);
            row.appendChild(actions);

            itemsListContainer.appendChild(row);
        });
    } catch (err) {
        console.error(err);
        itemsListContainer.innerHTML = "<p>Failed to load items.</p>";
    }
}

function fillItemFormForEdit(item) {
    itemIdInput.value = item.id;
    itemWarehouseIdInput.value = item.warehouse.id;
    itemNameInput.value = item.name;
    itemSkuInput.value = item.sku;
    itemQuantityInput.value = item.quantity;
    itemStorageLocationInput.value = item.storageLocation;
}

itemForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const warehouseId = Number(itemWarehouseIdInput.value);
    const itemId = itemIdInput.value;

    const payload = {
        name: itemNameInput.value.trim(),
        sku: itemSkuInput.value.trim(),
        quantity: Number(itemQuantityInput.value),
        storageLocation: itemStorageLocationInput.value.trim()
    };

    if (!payload.name || !payload.sku || isNaN(payload.quantity)) {
        alert("Please fill out all item fields");
        return;
    }

    try {
        if (itemId) {
            const updated = await apiPut(`/items/${itemId}`, {
                id: Number(itemId),
                ...payload,
                warehouse: { id: warehouseId }
            });
            console.log("Updated item", updated);
        } else {
            const created = await apiPost(`/items/warehouse/${warehouseId}`, payload);
            console.log("Created item", created);
        }

        itemIdInput.value = "";
        await loadItemsForWarehouse(warehouseId);
    } catch (err) {
        console.error(err);
        alert("Failed to save item. Check capacity and validation rules.");
    }
});

itemCancelBtn.addEventListener("click", () => closeModal(itemsModal));

addWarehouseBtn.addEventListener("click", () => openWarehouseModal(null));

loadWarehouses();
