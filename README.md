# GlobalTrade Logistics

GlobalTrade Logistics is a Jakarta EE 10 enterprise application for managing international supply-chain operations. It provides shipment visibility, vendor performance monitoring, inventory replenishment signals, customs/compliance audit trails, operational alerting, and EJB performance telemetry through a Payara Server 6 EAR deployment.

The system is built as a real enterprise application with a split Maven architecture, a MySQL-backed persistence layer, protected business services, scheduled monitoring jobs, interceptor-based audit and telemetry, and a responsive operations dashboard.

## Platform Overview

GlobalTrade Logistics helps operations teams coordinate global logistics workflows across carriers, warehouses, suppliers, customs processes, and internal control teams.

Core capabilities:

- Track high-priority shipments and delivery risk
- Create shipments and update shipment lifecycle status
- Monitor delayed shipments automatically
- Review customs-sensitive shipments
- Track inventory stock health across warehouses
- Raise replenishment alerts when stock reaches reorder thresholds
- Score vendors and maintain vendor tiers
- Record compliance audit events for protected business operations
- Capture EJB service performance samples
- Operate the system through a dashboard built for logistics teams

## Technology Stack

- Java 17
- Jakarta EE 10
- Payara Server 6
- MySQL 8
- Maven multi-module build
- EJB 4.0
- Jakarta Persistence 3.1
- Jakarta RESTful Web Services
- Jakarta Bean Validation
- JUnit 5
- AssertJ
- HTML, CSS, and vanilla JavaScript

## Repository Structure

```text
globaltrade-logistics
|-- globaltrade-logistics-common
|-- globaltrade-logistics-security
|-- globaltrade-logistics-ejb
|-- globaltrade-logistics-web
|-- globaltrade-logistics-ear
|-- deploy
|   |-- mysql
|   `-- payara
`-- docs
```

## Modules

### `globaltrade-logistics-common`

Shared application contract module.

Contains:

- DTO records for API and service responses
- Command records for write operations
- Domain enums for shipment, inventory, alert, vendor, and transaction states
- Business exceptions
- Security role constants
- Local EJB business interfaces

### `globaltrade-logistics-ejb`

Enterprise business module.

Contains:

- JPA entities for vendors, shipments, inventory items, alerts, compliance audit events, and performance metrics
- Stateless EJB services for shipments, inventory, vendors, alerts, compliance, dashboard snapshots, and telemetry
- Startup singleton services for seed data and timer configuration
- Declarative and programmatic EJB timers
- Audit, performance, and validation interceptors
- Container-managed transactions
- Role-protected service methods
- Business policies such as shipment risk scoring

### `globaltrade-logistics-security`

Payara server security extension.

Contains:

- Custom JAAS login module for supply-chain authentication
- PBKDF2 password verification support
- Security schema verification tests
- Domain-lib JAR used by Payara before the EAR authenticates users

### `globaltrade-logistics-web`

Web and API module.

Contains:

- JAX-RS application under `/api`
- REST resources for dashboard, shipments, inventory, vendors, alerts, and compliance
- Consistent API response envelopes
- API exception mapping
- Security headers filter
- Responsive logistics operations dashboard
- Web asset tests

### `globaltrade-logistics-ear`

Enterprise archive module for Payara deployment.

Contains:

- EAR packaging configuration
- Jakarta EE 10 application descriptor
- Common library JAR
- EJB JAR
- Web WAR with context root `/globaltrade`

## Enterprise Architecture

### Timers

`SupplyChainTimerBean` runs automated operational monitoring:

- Shipment delay detection
- Inventory replenishment monitoring
- Vendor tier refresh through a persistent programmatic interval timer

### Interceptors

The EJB layer uses interceptors for cross-cutting concerns:

- `AuditInterceptor`: records compliance audit events
- `PerformanceInterceptor`: records service execution duration and outcome
- `VendorValidationInterceptor`: blocks invalid shipment commands before business logic runs

### Transactions

The service layer uses container-managed transactions:

- Write commands use `REQUIRED`
- Monitoring jobs use `REQUIRES_NEW`
- Read operations use `SUPPORTS`
- JPA entities use optimistic locking with `@Version`

### Security

Security is applied at both web and EJB boundaries:

- Web access is protected through `web.xml`
- Payara group mappings are configured in `glassfish-web.xml`
- EJB services use role annotations
- User identities and role memberships are read from MySQL
- The custom Payara JAAS login module validates PBKDF2 credential hashes
- Security headers are added to API responses

Defined roles:

- `GLOBALTRADE_ADMIN`
- `LOGISTICS_COORDINATOR`
- `WAREHOUSE_MANAGER`
- `CUSTOMS_AGENT`
- `VENDOR_REPRESENTATIVE`
- `CUSTOMER_PORTAL_USER`

### Persistence

The persistence unit is `globaltradePU` and uses the JTA datasource:

```text
jdbc/globaltradeDS
```

Main entities:

- `VendorEntity`
- `ShipmentEntity`
- `InventoryItemEntity`
- `AlertEntity`
- `ComplianceAuditEntity`
- `PerformanceMetricEntity`

## Dashboard

Dashboard URL after deployment:

```text
http://localhost:8080/globaltrade
```

The dashboard includes:

- Operational KPI cards
- Priority shipment risk table
- Open alert list and acknowledgement action
- Shipment creation form
- Shipment status update form
- Inventory item creation form
- Inventory adjustment form
- Vendor creation form
- Vendor score evaluation form
- Compliance audit timeline
- EJB performance telemetry view

## REST API

Base path:

```text
/globaltrade/api
```

Endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/dashboard` | Read the dashboard snapshot. |
| `GET` | `/shipments` | Read active shipments. |
| `POST` | `/shipments` | Create a shipment. |
| `PUT` | `/shipments/status` | Update shipment status. |
| `GET` | `/inventory` | Read inventory items. |
| `POST` | `/inventory` | Create an inventory item. |
| `PUT` | `/inventory` | Adjust inventory levels. |
| `GET` | `/vendors` | Read vendor performance data. |
| `POST` | `/vendors` | Create a vendor. |
| `PUT` | `/vendors/score` | Recalculate vendor score and tier. |
| `GET` | `/alerts` | Read open alerts. |
| `PUT` | `/alerts/{id}/acknowledge` | Acknowledge an alert. |
| `GET` | `/compliance?limit=25` | Read recent compliance audit events. |

API responses use `ApiEnvelope<T>`:

```json
{
  "success": true,
  "message": "ok",
  "data": {},
  "generatedAt": "2026-08-27T00:00:00Z"
}
```

## Prerequisites

- Java 17
- Maven 3.9 or a Maven distribution bundled with an IDE
- Payara Server 6
- MySQL 8
- MySQL Connector/J copied into the Payara domain library directory

## Build

From the project root:

```bash
mvn clean verify
```

If Maven is not on `PATH`, use the Maven distribution bundled with IntelliJ IDEA or NetBeans. On the current development machine, this command was used successfully:

```powershell
& 'C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.1/plugins/maven-plugin/lib/maven3/bin/mvn.cmd' clean verify
```

The deployable EAR is created at:

```text
globaltrade-logistics-ear/target/globaltrade-logistics.ear
```

## Database Setup

Database bootstrap script:

```text
deploy/mysql/schema.sql
```

The script creates:

- Database `globaltrade_logistics`
- Authentication users and role membership tables
- Login and security audit tables
- Logistics operation tables for vendors, shipments, inventory, alerts, compliance audit, and performance metrics
- Deployment audit table for deployment tracking

Create the database login with a local password that is not saved in the repository:

```sql
CREATE USER IF NOT EXISTS 'globaltrade_app'@'%' IDENTIFIED BY '<your-local-db-password>';
GRANT ALL PRIVILEGES ON globaltrade_logistics.* TO 'globaltrade_app'@'%';
FLUSH PRIVILEGES;
```

Then run the schema with a MySQL administrator account:

```bash
mysql -u root -p < deploy/mysql/schema.sql
```

## Payara Setup

Payara setup script:

```text
deploy/payara/setup-domain.asadmin
```

The script configures:

- JDBC connection pool `globaltradePool`
- JDBC resource `jdbc/globaltradeDS`
- MySQL-backed custom realm `globaltradeRealm`
- Custom supply-chain JAAS login module properties
- Payara password alias reference for the JDBC pool instead of a plain database password

Build and install the Payara login module before creating or using the realm:

```powershell
mvn -pl globaltrade-logistics-security package
.\deploy\payara\install-security-extension.ps1 -PayaraHome C:\Payara\payara6 -DomainName domain1
```

Restart Payara after installing the security extension:

```bash
asadmin restart-domain domain1
```

Run from the Payara `bin` directory:

```bash
asadmin start-domain
asadmin create-password-alias globaltrade.db.password
asadmin multimode --file C:/Users/neth/Documents/Projects/intelli_j-idea-projects/bcd-ii-final-project/globaltrade-logistics/deploy/payara/setup-domain.asadmin
asadmin ping-connection-pool globaltradePool
```

When `create-password-alias` prompts for the alias password, enter the same MySQL password used for `globaltrade_app`. The tracked Payara script only stores `${ALIAS=globaltrade.db.password}` for the JDBC pool. The custom JAAS module uses `jdbc/globaltradeDS`, so the realm does not need its own database password.

## Deploy

Build and deploy the EAR:

```bash
mvn clean verify
asadmin deploy --force=true globaltrade-logistics-ear/target/globaltrade-logistics.ear
```

Open:

```text
http://localhost:8080/globaltrade
```

## Seed Data

The application seeds representative operational data for a useful first run:

- Vendors across strategic, approved, and watchlist tiers
- Shipments across in-transit, customs-review, delayed, picked-up, and delivered states
- Inventory items across healthy, low-stock, replenishment, and stockout states
- Alerts for inventory, vendor, and routing conditions

This gives teams an immediate operational view after deployment.

## Testing

Automated tests cover:

- Security role constants
- Shipment risk scoring
- Stateless EJB annotation presence
- Timer callback annotation presence
- Custom JAAS password hashing and security schema verification
- REST resource CDI injection
- API exception mapping
- Dashboard section coverage

Run:

```bash
mvn clean verify
```

Run Payara container integration tests after Payara, MySQL, and the custom realm are configured:

```bash
mvn -Parquillian-payara -pl globaltrade-logistics-ear -am verify
```

Run dependency and supply-chain security checks:

```bash
mvn -Psecurity-scan verify
```

## Manual Smoke Test Checklist

After deployment, verify:

- The dashboard loads at `/globaltrade`
- A permitted user can sign in
- KPI cards show seeded operational data
- A shipment can be created
- Shipment status can be updated
- Inventory can be adjusted
- Vendor score can be recalculated
- Alerts can be acknowledged
- Compliance audit events are created
- Performance telemetry appears after service activity
- Unauthorized users cannot access protected views

## Documentation

Additional technical documentation:

- `docs/architecture.md`
- `docs/testing-strategy.md`
- `deploy/payara/README.md`

## Operational Notes

- `persistence.xml` uses non-destructive schema management. Apply `deploy/mysql/schema.sql` before deploying the EAR.
- Database passwords must be supplied locally through MySQL administration and Payara password aliases, not committed into setup scripts.
- Ensure MySQL Connector/J is available in the Payara domain before deploying the EAR.
- Live operations use the Jakarta REST API backed by EJB services and MySQL.
