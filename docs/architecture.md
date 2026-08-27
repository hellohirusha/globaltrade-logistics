# GlobalTrade Logistics Architecture

## Assignment Alignment

The solution is a Jakarta EE 10 EAR platform for GlobalTrade Logistics Corporation. It implements the requested supply-chain modernization scenario through four modules:

- `globaltrade-logistics-common`: DTOs, enums, exceptions, role constants, and EJB local business interfaces.
- `globaltrade-logistics-ejb`: JPA entities, EJB services, timers, interceptors, transaction boundaries, seed data, and business policies.
- `globaltrade-logistics-web`: JAX-RS API, security filters, exception mapping, and a finished operations dashboard.
- `globaltrade-logistics-ear`: deployable EAR assembly for Payara Server 6.

## Core Enterprise Components

Timer services are implemented in `SupplyChainTimerBean`. Declarative timers monitor shipment delays and inventory replenishment. A persistent programmatic interval timer performs vendor health sweeps, demonstrating both timer creation styles required by the assignment.

Interceptors are implemented through `AuditInterceptor`, `PerformanceInterceptor`, and `VendorValidationInterceptor`. The audit interceptor writes compliance events for customs and trade auditability. The performance interceptor records method duration samples for dashboard telemetry. The vendor validation interceptor prevents invalid shipment creation before business logic executes.

Transaction demarcation uses container-managed transactions. Command methods use `REQUIRED`, monitoring jobs use `REQUIRES_NEW`, and read-only queries use `SUPPORTS`. Entities use optimistic locking with `@Version` to protect concurrent logistics operations.

Security uses role constants in `LogisticsRoles`, web security constraints in `web.xml`, Payara realm mappings in `glassfish-web.xml`, and EJB-level `@RolesAllowed` annotations. The deployment script creates the file realm and operational groups.

Exception handling separates business rule errors, missing resources, and unexpected platform errors. REST clients receive consistent `ApiEnvelope` responses through `ApiExceptionMapper`, while EJB services preserve rollback behavior for failed command operations.

Deployment follows a professional EAR architecture with a common library JAR, an EJB module, and a WAR module. Payara receives a JTA datasource named `jdbc/globaltradeDS`, backed by MySQL.

## Data Model

The persistence model contains:

- `VendorEntity`: vendor score, tier, country, activation status, and evaluation timestamp.
- `ShipmentEntity`: route, carrier, vendor, status, priority, customs reference, risk score, and delivery target.
- `InventoryItemEntity`: SKU, warehouse, quantity, reorder controls, stock status, and optimistic version.
- `AlertEntity`: severity, operational message, acknowledgement state, and raise time.
- `ComplianceAuditEntity`: actor, action, resource, outcome, source, and audit timestamp.
- `PerformanceMetricEntity`: intercepted operation, duration, outcome, and capture time.

## Operational Dashboard

The web dashboard supports:

- Live operational KPIs for active shipments, delayed shipments, customs reviews, inventory signals, critical alerts, and vendor watchlist count.
- Priority shipment table ordered by risk.
- Alert acknowledgement.
- Shipment creation and status updates.
- Inventory adjustment.
- Vendor scoring and tier refresh.
- Compliance audit trail and performance telemetry.

The UI includes bundled demo data so the interface remains reviewable before Payara and MySQL are running, while the same screen automatically switches to live API data when deployed.
