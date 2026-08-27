CREATE DATABASE IF NOT EXISTS globaltrade_logistics
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'globaltrade_app'@'%' IDENTIFIED BY 'GlobalTrade#2026!';
GRANT ALL PRIVILEGES ON globaltrade_logistics.* TO 'globaltrade_app'@'%';
FLUSH PRIVILEGES;

USE globaltrade_logistics;

CREATE TABLE IF NOT EXISTS vendors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vendorCode VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    country VARCHAR(80) NOT NULL,
    score DECIMAL(5, 2) NOT NULL,
    tier VARCHAR(24) NOT NULL,
    active TINYINT(1) NOT NULL,
    lastEvaluated DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_vendors_vendor_code UNIQUE (vendorCode),
    INDEX idx_vendors_tier_score (tier, score),
    INDEX idx_vendors_active (active)
);

CREATE TABLE IF NOT EXISTS inventory_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(60) NOT NULL,
    name VARCHAR(160) NOT NULL,
    warehouseCode VARCHAR(40) NOT NULL,
    quantityOnHand INT NOT NULL,
    reorderPoint INT NOT NULL,
    reorderQuantity INT NOT NULL,
    status VARCHAR(24) NOT NULL,
    lastUpdated DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_inventory_sku_warehouse UNIQUE (sku, warehouseCode),
    INDEX idx_inventory_status (status),
    INDEX idx_inventory_warehouse (warehouseCode)
);

CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reference VARCHAR(48) NOT NULL,
    origin VARCHAR(120) NOT NULL,
    destination VARCHAR(120) NOT NULL,
    carrier VARCHAR(120) NOT NULL,
    vendor_id BIGINT NOT NULL,
    status VARCHAR(24) NOT NULL,
    priority VARCHAR(24) NOT NULL,
    customsReference VARCHAR(80) NOT NULL,
    riskScore INT NOT NULL,
    estimatedDelivery DATETIME(6) NOT NULL,
    createdAt DATETIME(6) NOT NULL,
    lastUpdated DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_shipments_reference UNIQUE (reference),
    CONSTRAINT fk_shipments_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (id),
    INDEX idx_shipments_status (status),
    INDEX idx_shipments_priority (priority),
    INDEX idx_shipments_estimated_delivery (estimatedDelivery)
);

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    severity VARCHAR(16) NOT NULL,
    title VARCHAR(140) NOT NULL,
    message VARCHAR(800) NOT NULL,
    acknowledged TINYINT(1) NOT NULL,
    raisedAt DATETIME(6) NOT NULL,
    INDEX idx_alerts_open_severity (acknowledged, severity),
    INDEX idx_alerts_raised_at (raisedAt)
);

CREATE TABLE IF NOT EXISTS compliance_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor VARCHAR(120) NOT NULL,
    action VARCHAR(120) NOT NULL,
    resource VARCHAR(160) NOT NULL,
    outcome VARCHAR(40) NOT NULL,
    ipAddress VARCHAR(64) NOT NULL,
    createdAt DATETIME(6) NOT NULL,
    INDEX idx_compliance_created_at (createdAt),
    INDEX idx_compliance_resource_outcome (resource, outcome)
);

CREATE TABLE IF NOT EXISTS performance_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operation VARCHAR(160) NOT NULL,
    durationMillis BIGINT NOT NULL,
    outcome VARCHAR(40) NOT NULL,
    capturedAt DATETIME(6) NOT NULL,
    INDEX idx_performance_captured_at (capturedAt),
    INDEX idx_performance_operation (operation)
);

CREATE TABLE IF NOT EXISTS deployment_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    deployed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    artifact_name VARCHAR(160) NOT NULL,
    deployed_by VARCHAR(120) NOT NULL,
    notes VARCHAR(500) NOT NULL
);
