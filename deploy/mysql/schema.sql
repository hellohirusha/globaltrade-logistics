CREATE DATABASE IF NOT EXISTS globaltrade_logistics
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

-- Create the database login outside this tracked schema using a local secret:
-- CREATE USER IF NOT EXISTS 'globaltrade_app'@'%' IDENTIFIED BY '<local-secret>';
-- GRANT ALL PRIVILEGES ON globaltrade_logistics.* TO 'globaltrade_app'@'%';
-- FLUSH PRIVILEGES;

USE globaltrade_logistics;

CREATE TABLE IF NOT EXISTS auth_users (
    username VARCHAR(80) PRIMARY KEY,
    display_name VARCHAR(160) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(128) NOT NULL,
    password_iterations INT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    account_locked TINYINT(1) NOT NULL DEFAULT 0,
    failed_attempts INT NOT NULL DEFAULT 0,
    password_expires_at DATETIME(6) NULL,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_auth_users_active_locked (active, account_locked)
);

CREATE TABLE IF NOT EXISTS auth_user_roles (
    username VARCHAR(80) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    PRIMARY KEY (username, role_name),
    CONSTRAINT fk_auth_roles_user FOREIGN KEY (username) REFERENCES auth_users (username) ON DELETE CASCADE,
    INDEX idx_auth_roles_role_name (role_name)
);

CREATE TABLE IF NOT EXISTS login_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    outcome VARCHAR(40) NOT NULL,
    source_ip VARCHAR(64) NOT NULL,
    user_agent VARCHAR(160) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_login_audit_username_created (username, created_at),
    INDEX idx_login_audit_outcome_created (outcome, created_at)
);

CREATE TABLE IF NOT EXISTS security_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(80) NOT NULL,
    severity VARCHAR(24) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    details VARCHAR(800) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_security_events_type_created (event_type, created_at),
    INDEX idx_security_events_severity_created (severity, created_at)
);

INSERT INTO auth_users (username, display_name, password_hash, password_salt, password_iterations, active, account_locked)
VALUES
    ('admin', 'Operations Admin', 'o6Ny+yG45fNuXrfkSrF8ymEtiB2EOSoRP2u6TZ1ithI=', 'Z2xvYmFsdHJhZGUtYWRtaW4tMjAyNg==', 120000, 1, 0),
    ('coordinator', 'Logistics Coordinator', '68iLtMB8q/O22GUfM9soJGDR21gFUQQcQCqJszuyIIg=', 'Z2xvYmFsdHJhZGUtY29vcmRpbmF0b3ItMjAyNg==', 120000, 1, 0),
    ('warehouse', 'Warehouse Manager', 'EFJhVfD01QO5X9KcmQ7gb2xQlExErpzHuQ7eb90RoQg=', 'Z2xvYmFsdHJhZGUtd2FyZWhvdXNlLTIwMjY=', 120000, 1, 0),
    ('customs', 'Customs Agent', 'qjIq0w+vLkfu1NjhJ0D7uPlNy9oo3EUZimPKNPM7fSI=', 'Z2xvYmFsdHJhZGUtY3VzdG9tcy0yMDI2', 120000, 1, 0),
    ('vendor', 'Vendor Representative', 'ln8ZUvPp3+jk4Z0VxOFEZSUUv7foh4fGVOdFizFex8k=', 'Z2xvYmFsdHJhZGUtdmVuZG9yLTIwMjY=', 120000, 1, 0),
    ('customer', 'Customer Portal User', 'hJRPd2GgOkQuJwWP6I34bVrEGEpW5LdX0rh/EKT6Guk=', 'Z2xvYmFsdHJhZGUtY3VzdG9tZXItMjAyNg==', 120000, 1, 0)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO auth_user_roles (username, role_name)
VALUES
    ('admin', 'GLOBALTRADE_ADMIN'),
    ('admin', 'LOGISTICS_COORDINATOR'),
    ('admin', 'WAREHOUSE_MANAGER'),
    ('admin', 'CUSTOMS_AGENT'),
    ('coordinator', 'LOGISTICS_COORDINATOR'),
    ('warehouse', 'WAREHOUSE_MANAGER'),
    ('customs', 'CUSTOMS_AGENT'),
    ('vendor', 'VENDOR_REPRESENTATIVE'),
    ('customer', 'CUSTOMER_PORTAL_USER')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

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
