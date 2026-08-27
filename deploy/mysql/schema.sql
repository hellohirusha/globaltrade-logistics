CREATE DATABASE IF NOT EXISTS globaltrade_logistics
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'globaltrade_app'@'%' IDENTIFIED BY 'GlobalTrade#2026!';
GRANT ALL PRIVILEGES ON globaltrade_logistics.* TO 'globaltrade_app'@'%';
FLUSH PRIVILEGES;

USE globaltrade_logistics;

CREATE TABLE IF NOT EXISTS deployment_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    deployed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rtifact_name VARCHAR(160) NOT NULL,
    deployed_by VARCHAR(120) NOT NULL,
    notes VARCHAR(500) NOT NULL
    );
