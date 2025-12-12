const API_BASE_URL = "http://localhost:8080";

const warehouseCardsContainer = document.getElementById("warehouse-cards");
const warehouseSearchInput = document.getElementById("warehouse-search");
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
const itemSearchInput = document.getElementById("item-search");
const itemsAddBtn = document.getElementById("items-add-btn");
const itemsCloseBtn = document.getElementById("items-close-btn");

const itemFormModal = document.getElementById("item-form-modal");
const itemFormModalTitle = document.getElementById("item-form-modal-title");
const itemForm = document.getElementById("item-form");
const itemIdInput = document.getElementById("item-id");
const itemWarehouseIdInput = document.getElementById("item-warehouse-id");
const itemNameInput = document.getElementById("item-name");
const itemSkuInput = document.getElementById("item-sku");
const itemCategoryInput = document.getElementById("item-category");
const itemQuantityInput = document.getElementById("item-quantity");
const itemStorageLocationInput = document.getElementById("item-storage-location");
const itemCancelBtn = document.getElementById("item-cancel-btn");

const transferModal = document.getElementById("transfer-modal");
const transferForm = document.getElementById("transfer-form");
const transferInventoryIdInput = document.getElementById("transfer-inventory-id");
const transferFromWarehouseIdInput = document.getElementById("transfer-from-warehouse-id");
const transferToWarehouseSelect = document.getElementById("transfer-to-warehouse");
const transferQuantityInput = document.getElementById("transfer-quantity");
const transferCancelBtn = document.getElementById("transfer-cancel-btn");

const overlay = document.getElementById("overlay");

let allWarehouses = [];
let currentWarehouseForItems = null;
let lastLoadedItems = [];
let departments = [];

function openModal(modal) {
  modal.classList.remove("hidden");
  overlay.classList.remove("hidden");
}

function closeModal(modal) {
  modal.classList.add("hidden");
  overlay.classList.add("hidden");
}

overlay?.addEventListener("click", () => {
  closeModal(warehouseModal);
  closeModal(itemsModal);
  closeModal(itemFormModal);
  closeModal(transferModal);
});

async function api(path, options = {}) {
  const res = await fetch(API_BASE_URL + path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    }
  });

  const text = await res.text().catch(() => "");

  if (!res.ok) {
    throw new Error(text || `Request failed: ${res.status}`);
  }

  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function loadDepartments() {
  try {
    departments = await api("/products/departments"); 
  } catch (e) {
    departments = [];
  }
}

function fillDepartmentSelectIfPossible() {
  if (!itemCategoryInput) return;

  const tag = itemCategoryInput.tagName.toLowerCase();

  if (!departments.length) return;

  if (tag === "select") {
    itemCategoryInput.innerHTML = `<option value="">Select department</option>`;
    departments.forEach(dep => {
      const opt = document.createElement("option");
      opt.value = dep;
      opt.textContent = dep;
      itemCategoryInput.appendChild(opt);
    });
  } else {
    let dl = document.getElementById("department-datalist");
    if (!dl) {
      dl = document.createElement("datalist");
      dl.id = "department-datalist";
      document.body.appendChild(dl);
    }
    dl.innerHTML = "";
    departments.forEach(dep => {
      const opt = document.createElement("option");
      opt.value = dep;
      dl.appendChild(opt);
    });
    itemCategoryInput.setAttribute("list", "department-datalist");
  }
}

async function loadWarehouses() {
  allWarehouses = await api("/warehouses");
  renderWarehouses(allWarehouses);
}

async function renderWarehouses(list) {
  warehouseCardsContainer.innerHTML = "";

  if (!list || !list.length) {
    warehouseCardsContainer.innerHTML = "<p>No warehouses yet.</p>";
    return;
  }

  for (const w of list) {
    let load = 0;
    let remaining = 0;

    try {
      [load, remaining] = await Promise.all([
        api(`/warehouses/${w.id}/current-load`),
        api(`/warehouses/${w.id}/capacity`)
      ]);
    } catch (e) {
    }

    const card = document.createElement("div");
    card.className = "card";
    card.innerHTML = `
      <div class="card-header">
        <div class="card-header-main">
          <h3>${escapeHtml(w.name)}</h3>
          <div class="card-subtitle">${escapeHtml(w.location)}</div>
        </div>
        <button class="btn secondary edit-btn">Edit</button>
      </div>

      <div class="card-metrics">
        <p><strong>Max capacity:</strong> ${w.max_capacity}</p>
        <p><strong>Current load:</strong> ${load}</p>
        <p><strong>Remaining:</strong> ${remaining}</p>
      </div>

      <div class="card-actions">
        <button class="btn secondary view-btn">View items</button>
        <button class="btn secondary add-btn">Add item</button>
        <button class="btn danger delete-btn">Delete</button>
      </div>
    `;

    card.querySelector(".view-btn").onclick = () => openItemsModal(w);
    card.querySelector(".add-btn").onclick = () => {
      currentWarehouseForItems = w;
      openItemFormModal(null);
    };
    card.querySelector(".edit-btn").onclick = () => openWarehouseModal(w);
    card.querySelector(".delete-btn").onclick = async () => {
      if (!confirm(`Delete warehouse "${w.name}"?`)) return;
      try {
        await api(`/warehouses/${w.id}`, { method: "DELETE" });
        await loadWarehouses();
      } catch (err) {
        alert(err.message || "Delete failed. Warehouse may still have inventory.");
      }
    };

    warehouseCardsContainer.appendChild(card);
  }
}

warehouseSearchInput?.addEventListener("input", () => {
  const q = (warehouseSearchInput.value || "").trim().toLowerCase();
  const filtered = allWarehouses.filter(w =>
    (w.name || "").toLowerCase().includes(q) ||
    (w.location || "").toLowerCase().includes(q)
  );
  renderWarehouses(filtered);
});

function openWarehouseModal(w = null) {
  warehouseModalTitle.textContent = w ? "Edit Warehouse" : "Add Warehouse";
  warehouseIdInput.value = w?.id || "";
  warehouseNameInput.value = w?.name || "";
  warehouseLocationInput.value = w?.location || "";
  warehouseMaxCapacityInput.value = (w?.max_capacity ?? "");
  openModal(warehouseModal);
}

addWarehouseBtn?.addEventListener("click", () => openWarehouseModal());

warehouseCancelBtn?.addEventListener("click", () => closeModal(warehouseModal));

warehouseForm?.addEventListener("submit", async (e) => {
  e.preventDefault();

  const payload = {
    name: warehouseNameInput.value.trim(),
    location: warehouseLocationInput.value.trim(),
    max_capacity: Number(warehouseMaxCapacityInput.value)
  };

  if (!payload.name || !payload.location || Number.isNaN(payload.max_capacity)) {
    alert("Please fill out Name, Location, and Max Capacity.");
    return;
  }

  try {
    if (warehouseIdInput.value) {
      await api(`/warehouses/${warehouseIdInput.value}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    } else {
      await api(`/warehouses`, {
        method: "POST",
        body: JSON.stringify(payload)
      });
    }
    closeModal(warehouseModal);
    await loadWarehouses();
  } catch (err) {
    alert(err.message || "Failed to save warehouse");
  }
});

itemsCloseBtn?.addEventListener("click", () => closeModal(itemsModal));

itemsAddBtn?.addEventListener("click", () => openItemFormModal(null));

async function openItemsModal(warehouse) {
  currentWarehouseForItems = warehouse;
  itemsModalTitle.textContent = `Items in ${warehouse.name}`;
  if (itemSearchInput) itemSearchInput.value = "";
  await loadItemsForWarehouse(warehouse.id);
  openModal(itemsModal);
}

async function loadItemsForWarehouse(warehouseId) {
  lastLoadedItems = await api(`/inventory/warehouse/${warehouseId}`);
  renderItems(lastLoadedItems);
}

itemSearchInput?.addEventListener("input", () => {
  const q = (itemSearchInput.value || "").trim().toLowerCase();
  const filtered = lastLoadedItems.filter(inv => {
    const name = (inv.product?.name || "").toLowerCase();
    const sku = (inv.product?.sku || "").toLowerCase();
    const dept = (inv.product?.category || "").toLowerCase();
    return name.includes(q) || sku.includes(q) || dept.includes(q);
  });
  renderItems(filtered);
});

function renderItems(items) {
  itemsListContainer.innerHTML = "";

  if (!items || !items.length) {
    itemsListContainer.innerHTML = "<p>No items</p>";
    return;
  }

  for (const inv of items) {
    const row = document.createElement("div");
    row.className = "item-row";

    row.innerHTML = `
      <div class="item-row-main">
        <span><strong>${escapeHtml(inv.product?.name || "Unknown")}</strong> (SKU: ${escapeHtml(inv.product?.sku || "")})</span>
        <span>Department: ${escapeHtml(inv.product?.category || "N/A")} | Qty: ${inv.quantity} | Location: ${escapeHtml(inv.storageLocation || "")}</span>
      </div>
      <div class="item-row-actions">
        <button class="btn secondary edit-btn">Edit</button>
        <button class="btn secondary transfer-btn">Transfer</button>
        <button class="btn danger delete-btn">Delete</button>
      </div>
    `;

    row.querySelector(".edit-btn").onclick = () => openItemFormModal(inv);
    row.querySelector(".transfer-btn").onclick = () => openTransferModal(inv);
    row.querySelector(".delete-btn").onclick = async () => {
      if (!confirm(`Delete "${inv.product?.name || "item"}"?`)) return;
      try {
        await api(`/inventory/${inv.id}`, { method: "DELETE" });
        await loadItemsForWarehouse(currentWarehouseForItems.id);
        await loadWarehouses();
      } catch (err) {
        alert(err.message || "Failed to delete item");
      }
    };

    itemsListContainer.appendChild(row);
  }
}

itemCancelBtn?.addEventListener("click", () => {
  closeModal(itemFormModal);
  if (currentWarehouseForItems) openModal(itemsModal);
});

function openItemFormModal(inv) {
  if (!currentWarehouseForItems) {
    alert("Open a warehouse first.");
    return;
  }

  const isEdit = !!inv;

  itemFormModalTitle.textContent = isEdit ? "Edit Item" : "Add Item";
  itemIdInput.value = inv?.id || "";
  itemWarehouseIdInput.value = currentWarehouseForItems.id;

  itemNameInput.value = inv?.product?.name || "";
  itemSkuInput.value = inv?.product?.sku || "";
  itemCategoryInput.value = inv?.product?.category || "";
  itemQuantityInput.value = (inv?.quantity ?? "");
  itemStorageLocationInput.value = inv?.storageLocation || "";

  itemNameInput.disabled = isEdit;
  itemSkuInput.disabled = isEdit;
  itemCategoryInput.disabled = isEdit;

  closeModal(itemsModal);
  openModal(itemFormModal);
}

itemForm?.addEventListener("submit", async (e) => {
  e.preventDefault();

  const warehouseId = Number(itemWarehouseIdInput.value);
  const invId = itemIdInput.value;

  try {
    if (invId) {
      const payload = {
        quantity: Number(itemQuantityInput.value),
        storageLocation: itemStorageLocationInput.value.trim()
      };

      if (Number.isNaN(payload.quantity) || payload.quantity < 0 || !payload.storageLocation) {
        alert("Quantity must be 0 or more and Storage Location is required.");
        return;
      }

      await api(`/inventory/${invId}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    } else {
      // CREATE
      const payload = {
        sku: itemSkuInput.value.trim(),
        name: itemNameInput.value.trim(),
        description: "",
        category: (itemCategoryInput.value || "").trim(), // must match enum e.g. DAIRY
        quantity: Number(itemQuantityInput.value),
        storageLocation: itemStorageLocationInput.value.trim()
      };

      if (!payload.sku || !payload.name || !payload.category || Number.isNaN(payload.quantity) || payload.quantity <= 0 || !payload.storageLocation) {
        alert("Fill out Name, SKU, Department, Quantity (must be > 0), and Storage Location.");
        return;
      }

      const remaining = await api(`/warehouses/${warehouseId}/capacity`);
      if (payload.quantity > remaining) {
        alert(`Not enough capacity. Remaining: ${remaining}`);
        return;
      }

      await api(`/inventory/warehouse/${warehouseId}`, {
        method: "POST",
        body: JSON.stringify(payload)
      });
    }

    closeModal(itemFormModal);
    await loadItemsForWarehouse(currentWarehouseForItems.id);
    await loadWarehouses();
    openModal(itemsModal);
  } catch (err) {
    alert(err.message || "Failed to save item.");
  }
});

transferCancelBtn?.addEventListener("click", () => closeModal(transferModal));

function openTransferModal(inv) {
  if (!currentWarehouseForItems) return;

  transferInventoryIdInput.value = inv.id;
  transferFromWarehouseIdInput.value = currentWarehouseForItems.id;
  transferQuantityInput.value = "";

  transferToWarehouseSelect.innerHTML = "";

  const options = allWarehouses.filter(w => w.id !== currentWarehouseForItems.id);
  if (!options.length) {
    alert("Create a second warehouse to transfer items.");
    return;
  }

  for (const w of options) {
    const opt = document.createElement("option");
    opt.value = w.id;
    opt.textContent = `${w.name} (${w.location})`;
    transferToWarehouseSelect.appendChild(opt);
  }

  openModal(transferModal);
}

transferForm?.addEventListener("submit", async (e) => {
  e.preventDefault();

  const inventoryId = Number(transferInventoryIdInput.value);
  const fromWarehouse = Number(transferFromWarehouseIdInput.value);
  const toWarehouse = Number(transferToWarehouseSelect.value);
  const quantity = Number(transferQuantityInput.value);

  if (!inventoryId || !fromWarehouse || !toWarehouse || Number.isNaN(quantity) || quantity <= 0) {
    alert("Choose a destination warehouse and enter a quantity greater than 0.");
    return;
  }

  try {
    await api(`/inventory/${inventoryId}/transfer?fromWarehouse=${fromWarehouse}&toWarehouse=${toWarehouse}&quantity=${quantity}`, {
      method: "POST"
    });

    closeModal(transferModal);
    await loadItemsForWarehouse(currentWarehouseForItems.id);
    await loadWarehouses();
  } catch (err) {
    alert(err.message || "Transfer failed. Check capacity and quantity.");
  }
});

function escapeHtml(str) {
  return String(str ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

(async function init() {
  try {
    await loadDepartments();
    fillDepartmentSelectIfPossible();
    await loadWarehouses();
  } catch (err) {
    console.error(err);
    if (warehouseCardsContainer) {
      warehouseCardsContainer.innerHTML = "<p>Failed to load warehouses.</p>";
    }
  }
})();
