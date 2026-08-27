const emptySnapshot = {
    activeShipments: 0,
    delayedShipments: 0,
    customsReviews: 0,
    lowStockItems: 0,
    openCriticalAlerts: 0,
    watchlistVendors: 0,
    onTimeDeliveryRate: 0,
    averageVendorScore: 0,
    transactionSuccessRate: 0,
    generatedAt: new Date().toISOString(),
    priorityShipments: [],
    inventorySignals: [],
    openAlerts: [],
    performanceSamples: []
};

const roleTabs = {
    GLOBALTRADE_ADMIN: "overview",
    LOGISTICS_COORDINATOR: "shipments",
    WAREHOUSE_MANAGER: "inventory",
    CUSTOMS_AGENT: "compliance"
};

const state = {
    hasLoaded: false,
    snapshot: emptySnapshot,
    shipments: [],
    inventory: [],
    vendors: [],
    audit: [],
    errors: new Map()
};

const api = async (path, options = {}) => {
    const response = await fetch(`api${path}`, {
        cache: "no-store",
        credentials: "same-origin",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body?.message || `HTTP ${response.status}`);
    }
    if (!body?.success) {
        throw new Error(body.message || "Request failed");
    }
    return body.data;
};

const el = (id) => document.getElementById(id);
const setText = (id, value) => {
    const node = el(id);
    if (node) {
        node.textContent = value;
    }
};
const fmt = (value) => Number(value || 0).toLocaleString();
const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;"
}[character]));

function statusClass(value) {
    if (["CRITICAL", "DELAYED", "STOCKOUT", "SUSPENDED", "CANCELLED"].includes(value)) {
        return "danger";
    }
    if (["WARNING", "CUSTOMS_REVIEW", "REPLENISHMENT_DUE", "LOW_STOCK", "WATCHLIST", "HIGH"].includes(value)) {
        return "warning";
    }
    return "good";
}

function toast(message) {
    const toastEl = el("toast");
    toastEl.textContent = message;
    toastEl.classList.add("show");
    window.setTimeout(() => toastEl.classList.remove("show"), 3200);
}

function emptyRow(columns, message) {
    return `<tr><td class="empty-cell" colspan="${columns}">${message}</td></tr>`;
}

function emptyBlock(message) {
    return `<div class="empty-state">${message}</div>`;
}

function displayDate(value) {
    if (!value) {
        return "Not set";
    }
    return new Date(value).toLocaleString();
}

async function loadSection(key, request, apply) {
    try {
        const data = await request();
        apply(data);
        state.errors.delete(key);
    } catch (error) {
        state.errors.set(key, error.message);
    }
}

async function refresh() {
    await Promise.all([
        loadSection("dashboard", () => api("/dashboard"), (data) => {
            state.snapshot = { ...emptySnapshot, ...data };
        }),
        loadSection("shipments", () => api("/shipments"), (data) => {
            state.shipments = Array.isArray(data) ? data : [];
        }),
        loadSection("inventory", () => api("/inventory"), (data) => {
            state.inventory = Array.isArray(data) ? data : [];
        }),
        loadSection("vendors", () => api("/vendors"), (data) => {
            state.vendors = Array.isArray(data) ? data : [];
        }),
        loadSection("compliance", () => api("/compliance?limit=18"), (data) => {
            state.audit = Array.isArray(data) ? data : [];
        })
    ]);

    render();
    if (state.hasLoaded && state.errors.size > 0) {
        toast("Some live data could not be loaded. Check Payara, MySQL, and the server log.");
    }
    state.hasLoaded = true;
}

function renderHealth(snapshot) {
    const healthy = state.errors.size === 0;
    const everyEndpointFailed = state.errors.size >= 5;
    const statusLabel = healthy ? "System online" : everyEndpointFailed ? "Service check" : "Limited access";
    el("systemStatus").textContent = statusLabel;
    el("systemStatus").classList.toggle("warning", !healthy && !everyEndpointFailed);
    el("systemStatus").classList.toggle("offline", everyEndpointFailed);
    setText("healthScore", healthy ? `${snapshot.transactionSuccessRate || 0}%` : statusLabel);
    setText("onTimeRate", `${snapshot.onTimeDeliveryRate || 0}%`);
    setText("vendorScore", Number(snapshot.averageVendorScore || 0).toFixed(1));
    el("lastUpdated").textContent = healthy
        ? `Updated ${displayDate(snapshot.generatedAt)}`
        : `${state.errors.size} live section${state.errors.size === 1 ? "" : "s"} unavailable`;
}

function renderMetrics(snapshot) {
    el("activeShipments").textContent = fmt(snapshot.activeShipments);
    el("delayedShipments").textContent = fmt(snapshot.delayedShipments);
    el("customsReviews").textContent = fmt(snapshot.customsReviews);
    el("lowStockItems").textContent = fmt(snapshot.lowStockItems);
    el("openCriticalAlerts").textContent = fmt(snapshot.openCriticalAlerts);
    el("watchlistVendors").textContent = fmt(snapshot.watchlistVendors);

    el("networkSummary").textContent = `${state.shipments.length} active rows loaded`;
    el("issueSummary").textContent = Number(snapshot.delayedShipments || 0) > 0 ? "Immediate review needed" : "No delayed shipment pressure";
    el("stockSummary").textContent = Number(snapshot.lowStockItems || 0) > 0 ? "Reorder action required" : "Stock positions stable";
    el("supplierSummary").textContent = Number(snapshot.watchlistVendors || 0) > 0 ? "Supplier review active" : "Supplier network stable";
}

function shipmentRoute(shipment) {
    return `<span class="route-text">${escapeHtml(shipment.origin)} to ${escapeHtml(shipment.destination)}</span>`;
}

function renderShipmentRows() {
    el("shipmentRows").innerHTML = state.shipments.length ? state.shipments.map((shipment) => `
        <tr>
            <td><span class="reference-text">${escapeHtml(shipment.reference)}</span><br><span class="muted">${escapeHtml(shipment.carrier || "No carrier")}</span></td>
            <td>${shipmentRoute(shipment)}<br><span class="muted">${escapeHtml(shipment.vendorCode || "No vendor")}</span></td>
            <td><span class="pill ${statusClass(shipment.priority)}">${escapeHtml(shipment.priority)}</span></td>
            <td><span class="pill ${statusClass(shipment.status)}">${escapeHtml(shipment.status)}</span></td>
            <td><span class="metric-text">${escapeHtml(shipment.riskScore)}</span><br><span class="muted">${displayDate(shipment.estimatedDelivery)}</span></td>
        </tr>
    `).join("") : emptyRow(5, "No active live shipment records are available.");
}

function renderPriorityShipments(snapshot) {
    const shipments = snapshot.priorityShipments || [];
    el("priorityShipments").innerHTML = shipments.length ? shipments.map((shipment) => `
        <tr>
            <td><span class="reference-text">${escapeHtml(shipment.reference)}</span><br><span class="muted">${escapeHtml(shipment.carrier || "No carrier")}</span></td>
            <td>${shipmentRoute(shipment)}</td>
            <td><span class="pill ${statusClass(shipment.status)}">${escapeHtml(shipment.status)}</span></td>
            <td><span class="metric-text">${escapeHtml(shipment.riskScore)}</span></td>
        </tr>
    `).join("") : emptyRow(4, "No priority shipment exceptions are currently open.");
}

function renderAlerts(snapshot) {
    const alerts = snapshot.openAlerts || [];
    el("alertsList").innerHTML = alerts.length ? alerts.map((alert) => `
        <div class="alert-item ${statusClass(alert.severity)}">
            <div>
                <span class="pill ${statusClass(alert.severity)}">${escapeHtml(alert.severity)}</span>
                <strong>${escapeHtml(alert.title)}</strong>
                <p>${escapeHtml(alert.message)}</p>
                <p class="muted">Raised ${displayDate(alert.raisedAt)}</p>
            </div>
            <button type="button" data-ack="${escapeHtml(alert.id)}">Acknowledge</button>
        </div>
    `).join("") : emptyBlock("No open live alerts.");
}

function renderInventory() {
    el("inventoryRows").innerHTML = state.inventory.length ? state.inventory.map((item) => `
        <tr>
            <td><span class="reference-text">${escapeHtml(item.sku)}</span></td>
            <td>${escapeHtml(item.name)}</td>
            <td>${escapeHtml(item.warehouseCode)}</td>
            <td><span class="metric-text">${fmt(item.quantityOnHand)}</span></td>
            <td>${fmt(item.reorderPoint)} / ${fmt(item.reorderQuantity)}</td>
            <td><span class="pill ${statusClass(item.status)}">${escapeHtml(item.status)}</span></td>
        </tr>
    `).join("") : emptyRow(6, "No live inventory records are available.");
}

function renderVendors() {
    el("vendorRows").innerHTML = state.vendors.length ? state.vendors.map((vendor) => `
        <tr>
            <td><span class="reference-text">${escapeHtml(vendor.vendorCode)}</span></td>
            <td>${escapeHtml(vendor.name)}</td>
            <td>${escapeHtml(vendor.country)}</td>
            <td><span class="metric-text">${Number(vendor.score).toFixed(2)}</span></td>
            <td><span class="pill ${statusClass(vendor.tier)}">${escapeHtml(vendor.tier)}</span></td>
            <td><span class="pill ${vendor.active ? "good" : "danger"}">${vendor.active ? "ACTIVE" : "INACTIVE"}</span></td>
        </tr>
    `).join("") : emptyRow(6, "No live vendor records are available.");
}

function renderTelemetry(snapshot) {
    const samples = snapshot.performanceSamples || [];
    const max = Math.max(...samples.map((sample) => sample.durationMillis), 1);
    el("performanceBars").innerHTML = samples.length ? samples.map((sample) => `
        <div class="bar-row">
            <span class="bar-label" title="${escapeHtml(sample.operation)}">${escapeHtml(sample.operation)}</span>
            <span class="bar-track"><span class="bar-fill" style="width:${Math.max(6, sample.durationMillis / max * 100)}%"></span></span>
            <strong>${escapeHtml(sample.durationMillis)} ms</strong>
        </div>
    `).join("") : emptyBlock("No live telemetry samples have been recorded yet.");
}

function renderAudit() {
    el("auditList").innerHTML = state.audit.length ? state.audit.map((event) => `
        <div class="timeline-item ${statusClass(event.outcome)}">
            <strong>${escapeHtml(event.action)}</strong>
            <p>${escapeHtml(event.actor)} changed ${escapeHtml(event.resource)} with ${escapeHtml(event.outcome)} at ${displayDate(event.createdAt)}</p>
            <p class="muted">${escapeHtml(event.ipAddress || "No IP address captured")}</p>
        </div>
    `).join("") : emptyBlock("No live compliance events have been recorded yet.");
}

function selectedValue(id) {
    return el(id).value;
}

function option(value, label) {
    return `<option value="${escapeHtml(value)}">${escapeHtml(label)}</option>`;
}

function keepSelection(id, optionsMarkup, emptyLabel) {
    const select = el(id);
    const previous = select.value;
    select.innerHTML = optionsMarkup || option("", emptyLabel);
    if ([...select.options].some((item) => item.value === previous)) {
        select.value = previous;
    }
}

function populateSelects() {
    const activeVendors = state.vendors.filter((vendor) => vendor.active);
    keepSelection(
        "shipmentVendorSelect",
        activeVendors.map((vendor) => option(vendor.vendorCode, `${vendor.vendorCode} - ${vendor.name}`)).join(""),
        "No active vendors available"
    );
    keepSelection(
        "vendorScoreSelect",
        state.vendors.map((vendor) => option(vendor.vendorCode, `${vendor.vendorCode} - ${vendor.name}`)).join(""),
        "No vendors available"
    );
    keepSelection(
        "shipmentReferenceSelect",
        state.shipments.map((shipment) => option(shipment.reference, `${shipment.reference} - ${shipment.status}`)).join(""),
        "No active shipments available"
    );
    keepSelection(
        "inventoryItemSelect",
        state.inventory.map((item) => option(`${item.sku}||${item.warehouseCode}`, `${item.sku} - ${item.name}`)).join(""),
        "No inventory positions available"
    );
    syncInventorySelection();
}

function render() {
    const snapshot = state.snapshot;
    renderHealth(snapshot);
    renderMetrics(snapshot);
    renderShipmentRows();
    renderPriorityShipments(snapshot);
    renderAlerts(snapshot);
    renderInventory();
    renderVendors();
    renderTelemetry(snapshot);
    renderAudit();
    populateSelects();
}

function serialize(form) {
    return Object.fromEntries(new FormData(form).entries());
}

async function submitJson(path, method, form, transform = (data) => data) {
    const payload = transform(serialize(form));
    try {
        await api(path, { method, body: JSON.stringify(payload) });
        toast("Operation completed successfully.");
        form.reset();
        setDefaultDeliveryTime();
        syncInventorySelection();
        await refresh();
    } catch (error) {
        toast(`Operation failed: ${error.message}`);
    }
}

function syncInventorySelection() {
    const [sku = "", warehouseCode = ""] = selectedValue("inventoryItemSelect").split("||");
    const item = state.inventory.find((record) => record.sku === sku && record.warehouseCode === warehouseCode);
    el("inventorySku").value = sku;
    el("inventoryWarehouseCode").value = warehouseCode;
    el("inventoryWarehouse").value = warehouseCode;
    if (item) {
        document.querySelector("input[name='quantityOnHand']").value = item.quantityOnHand;
        document.querySelector("input[name='reorderPoint']").value = item.reorderPoint;
        document.querySelector("input[name='reorderQuantity']").value = item.reorderQuantity;
    }
}

function activateTab(tabId) {
    const targetTab = document.querySelector(`.tab[data-tab="${tabId}"]:not([hidden])`) || document.querySelector(".tab:not([hidden])");
    if (!targetTab) {
        return;
    }
    document.querySelectorAll(".tab").forEach((tab) => tab.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach((panel) => panel.classList.remove("active"));
    targetTab.classList.add("active");
    el(targetTab.dataset.tab).classList.add("active");
}

function applyRoleView() {
    const role = el("roleSelector").value;
    document.querySelectorAll(".tab").forEach((tab) => {
        tab.hidden = !tab.dataset.roles.split(",").includes(role);
    });
    const activeTab = document.querySelector(".tab.active");
    if (!activeTab || activeTab.hidden) {
        activateTab(roleTabs[role]);
    }
}

function setDefaultDeliveryTime() {
    const deliveryInput = document.querySelector("input[name='estimatedDelivery']");
    deliveryInput.value = new Date(Date.now() + 36 * 60 * 60 * 1000).toISOString().slice(0, 16);
}

document.querySelectorAll(".tab").forEach((button) => {
    button.addEventListener("click", () => activateTab(button.dataset.tab));
});

el("roleSelector").addEventListener("change", applyRoleView);
el("refreshButton").addEventListener("click", refresh);
el("inventoryItemSelect").addEventListener("change", syncInventorySelection);

el("alertsList").addEventListener("click", async (event) => {
    const button = event.target.closest("[data-ack]");
    if (!button) {
        return;
    }
    try {
        await api(`/alerts/${button.dataset.ack}/acknowledge`, { method: "PUT" });
        toast("Alert acknowledged.");
        await refresh();
    } catch (error) {
        toast(`Operation failed: ${error.message}`);
    }
});

el("shipmentForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/shipments", "POST", event.currentTarget);
});

el("statusForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/shipments/status", "PUT", event.currentTarget);
});

el("inventoryCreateForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/inventory", "POST", event.currentTarget, (data) => ({
        ...data,
        quantityOnHand: Number(data.quantityOnHand),
        reorderPoint: Number(data.reorderPoint),
        reorderQuantity: Number(data.reorderQuantity)
    }));
});

el("inventoryForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/inventory", "PUT", event.currentTarget, (data) => ({
        ...data,
        quantityOnHand: Number(data.quantityOnHand),
        reorderPoint: Number(data.reorderPoint),
        reorderQuantity: Number(data.reorderQuantity)
    }));
});

el("vendorCreateForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/vendors", "POST", event.currentTarget, (data) => ({
        ...data,
        score: Number(data.score),
        active: data.active === "true"
    }));
});

el("vendorForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/vendors/score", "PUT", event.currentTarget, (data) => ({
        ...data,
        onTimeScore: Number(data.onTimeScore),
        complianceScore: Number(data.complianceScore),
        disruptionScore: Number(data.disruptionScore)
    }));
});

setDefaultDeliveryTime();
applyRoleView();
refresh();
