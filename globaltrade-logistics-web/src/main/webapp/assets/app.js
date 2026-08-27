const fallback = {
    activeShipments: 4,
    delayedShipments: 1,
    customsReviews: 1,
    lowStockItems: 2,
    openCriticalAlerts: 1,
    watchlistVendors: 1,
    onTimeDeliveryRate: 20,
    averageVendorScore: 79.38,
    transactionSuccessRate: 100,
    generatedAt: new Date().toISOString(),
    priorityShipments: [
        { reference: "GTL-2026-0003", origin: "Shenzhen", destination: "Los Angeles", status: "DELAYED", riskScore: 100 },
        { reference: "GTL-2026-0002", origin: "Hamburg", destination: "Rotterdam", status: "CUSTOMS_REVIEW", riskScore: 73 },
        { reference: "GTL-2026-0001", origin: "Colombo", destination: "Singapore", status: "IN_TRANSIT", riskScore: 50 }
    ],
    inventorySignals: [
        { sku: "GT-SEAL-220", name: "Tamper proof customs seal", warehouseCode: "HAM-WH-03", quantityOnHand: 0, status: "STOCKOUT" },
        { sku: "GT-SENSOR-500", name: "IoT shipment sensor", warehouseCode: "SIN-WH-02", quantityOnHand: 14, status: "REPLENISHMENT_DUE" }
    ],
    openAlerts: [
        { id: 1, severity: "CRITICAL", title: "Customs seal stockout", message: "GT-SEAL-220 is out of stock at HAM-WH-03 and blocks export release." },
        { id: 2, severity: "WARNING", title: "Vendor watchlist", message: "VEN-CN-021 requires extra validation before new critical shipments." }
    ],
    performanceSamples: [
        { operation: "DashboardServiceBean.snapshot", durationMillis: 9, outcome: "SUCCESS" },
        { operation: "ShipmentServiceBean.monitorShipmentDelays", durationMillis: 16, outcome: "SUCCESS" }
    ]
};

const state = {
    usingFallback: false,
    snapshot: fallback,
    inventory: fallback.inventorySignals,
    vendors: [
        { vendorCode: "VEN-SG-001", name: "TransOcean Freight Network", country: "Singapore", score: 94.50, tier: "STRATEGIC" },
        { vendorCode: "VEN-DE-014", name: "Alpine Customs Brokerage", country: "Germany", score: 82.25, tier: "APPROVED" },
        { vendorCode: "VEN-CN-021", name: "Pacific Supplier Hub", country: "China", score: 61.40, tier: "WATCHLIST" }
    ],
    audit: []
};

const api = async (path, options = {}) => {
    const response = await fetch(`api${path}`, {
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    if (!response.ok) {
        throw new Error(`Request failed with HTTP ${response.status}`);
    }
    const body = await response.json();
    if (!body.success) {
        throw new Error(body.message || "Request failed");
    }
    return body.data;
};

const el = (id) => document.getElementById(id);
const fmt = (value) => Number(value || 0).toLocaleString();

function statusClass(value) {
    if (["CRITICAL", "DELAYED", "STOCKOUT", "SUSPENDED"].includes(value)) {
        return "danger";
    }
    if (["WARNING", "CUSTOMS_REVIEW", "REPLENISHMENT_DUE", "LOW_STOCK", "WATCHLIST"].includes(value)) {
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

async function refresh() {
    try {
        state.snapshot = await api("/dashboard");
        state.inventory = await api("/inventory");
        state.vendors = await api("/vendors");
        state.audit = await api("/compliance?limit=18");
        state.usingFallback = false;
    } catch (error) {
        state.snapshot = fallback;
        state.usingFallback = true;
        toast("Showing bundled demo data until Payara and MySQL are running.");
    }
    render();
}

function render() {
    const snapshot = state.snapshot;
    el("activeShipments").textContent = fmt(snapshot.activeShipments);
    el("delayedShipments").textContent = fmt(snapshot.delayedShipments);
    el("customsReviews").textContent = fmt(snapshot.customsReviews);
    el("lowStockItems").textContent = fmt(snapshot.lowStockItems);
    el("openCriticalAlerts").textContent = fmt(snapshot.openCriticalAlerts);
    el("watchlistVendors").textContent = fmt(snapshot.watchlistVendors);
    el("healthScore").textContent = `${snapshot.transactionSuccessRate || 100}%`;
    el("lastUpdated").textContent = state.usingFallback ? "Demo data" : `Updated ${new Date(snapshot.generatedAt).toLocaleString()}`;

    el("priorityShipments").innerHTML = snapshot.priorityShipments.map((shipment) => `
        <tr>
            <td><strong>${shipment.reference}</strong><br><span class="muted">${shipment.carrier || ""}</span></td>
            <td>${shipment.origin} to ${shipment.destination}</td>
            <td><span class="pill ${statusClass(shipment.status)}">${shipment.status}</span></td>
            <td><strong>${shipment.riskScore}</strong></td>
        </tr>
    `).join("");

    el("alertsList").innerHTML = snapshot.openAlerts.map((alert) => `
        <div class="alert-item">
            <div>
                <span class="pill ${statusClass(alert.severity)}">${alert.severity}</span>
                <strong>${alert.title}</strong>
                <p>${alert.message}</p>
            </div>
            <button type="button" data-ack="${alert.id}">Ack</button>
        </div>
    `).join("");

    el("inventoryRows").innerHTML = state.inventory.map((item) => `
        <tr>
            <td><strong>${item.sku}</strong></td>
            <td>${item.name}</td>
            <td>${item.warehouseCode}</td>
            <td>${fmt(item.quantityOnHand)}</td>
            <td><span class="pill ${statusClass(item.status)}">${item.status}</span></td>
        </tr>
    `).join("");

    el("vendorRows").innerHTML = state.vendors.map((vendor) => `
        <tr>
            <td><strong>${vendor.vendorCode}</strong></td>
            <td>${vendor.name}</td>
            <td>${vendor.country}</td>
            <td>${Number(vendor.score).toFixed(2)}</td>
            <td><span class="pill ${statusClass(vendor.tier)}">${vendor.tier}</span></td>
        </tr>
    `).join("");

    const samples = snapshot.performanceSamples.length ? snapshot.performanceSamples : fallback.performanceSamples;
    const max = Math.max(...samples.map((sample) => sample.durationMillis), 1);
    el("performanceBars").innerHTML = samples.map((sample) => `
        <div class="bar-row">
            <span class="bar-label" title="${sample.operation}">${sample.operation}</span>
            <span class="bar-track"><span class="bar-fill" style="width:${Math.max(6, sample.durationMillis / max * 100)}%"></span></span>
            <strong>${sample.durationMillis} ms</strong>
        </div>
    `).join("");

    el("auditList").innerHTML = (state.audit.length ? state.audit : [
        { actor: "system", action: "AuditInterceptor", resource: "EJB service", outcome: "SUCCESS", createdAt: new Date().toISOString() }
    ]).map((event) => `
        <div class="timeline-item">
            <strong>${event.action}</strong>
            <p>${event.actor} changed ${event.resource} with ${event.outcome} at ${new Date(event.createdAt).toLocaleString()}</p>
        </div>
    `).join("");
}

function serialize(form) {
    return Object.fromEntries(new FormData(form).entries());
}

async function submitJson(path, method, form, transform = (data) => data) {
    const payload = transform(serialize(form));
    try {
        await api(path, { method, body: JSON.stringify(payload) });
        toast("Operation completed successfully.");
        await refresh();
    } catch (error) {
        toast(error.message);
    }
}

document.querySelectorAll(".tab").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".tab").forEach((tab) => tab.classList.remove("active"));
        document.querySelectorAll(".tab-panel").forEach((panel) => panel.classList.remove("active"));
        button.classList.add("active");
        el(button.dataset.tab).classList.add("active");
    });
});

el("refreshButton").addEventListener("click", refresh);

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
        toast(error.message);
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

el("inventoryForm").addEventListener("submit", (event) => {
    event.preventDefault();
    submitJson("/inventory", "PUT", event.currentTarget, (data) => ({
        ...data,
        quantityOnHand: Number(data.quantityOnHand),
        reorderPoint: Number(data.reorderPoint),
        reorderQuantity: Number(data.reorderQuantity)
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

const deliveryInput = document.querySelector("input[name='estimatedDelivery']");
deliveryInput.value = new Date(Date.now() + 36 * 60 * 60 * 1000).toISOString().slice(0, 16);
refresh();
