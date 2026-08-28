# GlobalTrade Logistics Architecture

## Architecture Overview

The solution is a Jakarta EE 10 EAR platform for GlobalTrade Logistics Corporation. It implements supply-chain operations through dedicated Maven modules:

- `globaltrade-logistics-common`: DTOs, enums, exceptions, role constants, and EJB local business interfaces.
- `globaltrade-logistics-security`: Payara custom JAAS login module and security support for MySQL-backed authentication.
- `globaltrade-logistics-ejb`: JPA entities, EJB services, timers, interceptors, transaction boundaries, seed data, and business policies.
- `globaltrade-logistics-web`: JAX-RS API, security filters, exception mapping, and a finished operations dashboard.
- `globaltrade-logistics-ear`: deployable EAR assembly for Payara Server 6.

## Core Enterprise Components

Timer services are implemented in `SupplyChainTimerBean`. Declarative timers monitor shipment delays and inventory replenishment. A persistent programmatic interval timer performs vendor health sweeps.

Interceptors are implemented through `AuditInterceptor`, `PerformanceInterceptor`, and `VendorValidationInterceptor`. The audit interceptor writes compliance events for customs and trade auditability. The performance interceptor records method duration samples for dashboard telemetry. The vendor validation interceptor prevents invalid shipment creation before business logic executes.

Transaction demarcation uses container-managed transactions. Command methods use `REQUIRED`, monitoring jobs use `REQUIRES_NEW`, and read-only queries use `SUPPORTS`. Entities use optimistic locking with `@Version` to protect concurrent logistics operations.

Security uses role constants in `LogisticsRoles`, web security constraints in `web.xml`, Payara realm mappings in `glassfish-web.xml`, and EJB-level `@RolesAllowed` annotations. Payara delegates authentication to the custom `SupplyChainLoginModule`, which verifies MySQL-backed PBKDF2 password hashes and maps users to operational groups. The login module reads the configured `jdbc/globaltradeDS` datasource, while the datasource password is referenced through a Payara password alias so tracked configuration stores an alias token rather than a cleartext secret.

Exception handling separates business rule errors, missing resources, and unexpected platform errors. REST clients receive consistent `ApiEnvelope` responses through `ApiExceptionMapper`, while EJB services preserve rollback behavior for failed command operations.

Deployment follows a professional EAR architecture with a common library JAR, an EJB module, and a WAR module. Payara receives a JTA datasource named `jdbc/globaltradeDS`, backed by MySQL. Environment-specific database passwords are supplied during server setup through `create-password-alias` and are kept outside source control.

## Data Model

The persistence model contains:

- `VendorEntity`: vendor score, tier, country, activation status, and evaluation timestamp.
- `ShipmentEntity`: route, carrier, vendor, status, priority, customs reference, risk score, and delivery target.
- `InventoryItemEntity`: SKU, warehouse, quantity, reorder controls, stock status, and optimistic version.
- `AlertEntity`: severity, operational message, acknowledgement state, and raise time.
- `ComplianceAuditEntity`: actor, action, resource, outcome, source, and audit timestamp.
- `PerformanceMetricEntity`: intercepted operation, duration, outcome, and capture time.
- `auth_users`: MySQL-backed application users, credential hashes, lockout state, and login metadata.
- `auth_user_roles`: role memberships used by Payara and EJB authorization.
- `login_audit` and `security_events`: authentication and security event evidence.

## Operational Dashboard

The web dashboard supports:

- Live operational KPIs for active shipments, delayed shipments, customs reviews, inventory signals, critical alerts, and vendor watchlist count.
- Priority shipment table ordered by risk.
- Alert acknowledgement.
- Shipment creation and status updates.
- Inventory item creation and adjustment.
- Vendor creation, scoring, and tier refresh.
- Compliance audit trail and performance telemetry.

The UI reads live operational data through the JAX-RS API and reports service availability through dashboard status indicators.
