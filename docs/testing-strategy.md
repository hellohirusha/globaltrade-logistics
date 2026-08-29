# Testing Strategy

## Scope

The test approach validates timer services, interceptors, transaction behavior, supply-chain security, performance evidence, exception handling, and deployment verification.

## Automated Tests Included

- Common module role contract test verifies enterprise role constants.
- Checkstyle verifies Java source and test-source hygiene across the Maven reactor.
- Security module tests verify PBKDF2 credential hashes, custom JAAS configuration, and database security tables.
- EJB risk scoring tests validate high-risk and low-risk logistics scenarios.
- EJB annotation tests verify stateless service and timer callback presence.
- EJB interceptor tests verify shipment validation behavior.
- Web resource tests verify CDI injection on REST resources.
- API exception mapper tests verify business, not-found, access, and platform error responses.
- Web asset test verifies that the dashboard contains operational sections for shipments, inventory, vendors, compliance, and telemetry.
- Arquillian integration tests verify authenticated and unauthorized API behavior in Payara.

## Manual End-to-End Test Cases

| ID | Scenario | Expected Result |
| --- | --- | --- |
| E2E-01 | Sign in to `/globaltrade` as `admin` | Dashboard loads and shows seeded shipment, vendor, inventory, alert, compliance, and performance data. |
| E2E-02 | Create shipment `GTL-2026-0099` with vendor `VEN-SG-001` | New shipment appears in priority shipments and audit trail records the EJB call. |
| E2E-03 | Update `GTL-2026-0002` to `CUSTOMS_REVIEW` | Status persists, warning alert is raised, and transaction commits atomically. |
| E2E-04 | Adjust `GT-SENSOR-500` below reorder point | Inventory status becomes `REPLENISHMENT_DUE` and an alert is generated. |
| E2E-05 | Score `VEN-CN-021` below threshold | Vendor tier remains or moves to `WATCHLIST`, with alert evidence. |
| E2E-06 | Attempt dashboard access with a user outside configured groups | Payara denies access before the dashboard or API returns data. |
| E2E-07 | Stop MySQL during a command request | Request fails, command transaction rolls back, and Payara logs the system exception. |

## Performance Validation

The `PerformanceInterceptor` records EJB service durations to the `performance_metrics` table and exposes them on the dashboard. Use the dashboard telemetry plus Payara request logs to validate:

- Dashboard snapshot response under 500 ms with seeded data.
- Shipment create/update under 750 ms on local MySQL.
- Timer monitor jobs completing without long-running transaction locks.
- No failed metrics during normal operational flows.

## Security Validation

Security is validated through:

- Payara custom JAAS authentication.
- MySQL-backed users and role memberships.
- PBKDF2 password hash verification.
- Payara password alias usage for database credentials.
- Account lock and login audit records.
- Group to role mappings in `glassfish-web.xml`.
- Web constraint on all dashboard and API paths.
- EJB method-level role restrictions.
- Compliance audit records for command methods.

## Container Integration Tests

Run these after MySQL is bootstrapped, the security extension is installed into Payara, the `globaltrade.db.password` Payara alias exists, and the domain is running. Supply the test user password locally:

```powershell
mvn -Parquillian-payara -pl globaltrade-logistics-ear -am "-Dglobaltrade.it.password=<local-test-password>" verify
```

The Arquillian profile deploys the packaged EAR to Payara and verifies live HTTP behavior for admin, coordinator, warehouse, customs, anonymous access, invalid shipment routes, and duplicate vendor submissions.

## Supply Chain Security Scan

Run:

```powershell
$env:NVD_API_KEY = "<your-nvd-api-key>"
mvn -Psecurity-scan verify
```

The profile runs Maven Enforcer, OWASP Dependency-Check, and CycloneDX SBOM generation. Dependency-Check requires vulnerability database downloads, so the first run can take several minutes. OWASP recommends configuring the NVD API key through an environment variable or Maven settings rather than putting it directly in `pom.xml`.

Run local Enforcer and SBOM validation without the NVD refresh when an API key is not available:

```powershell
mvn -Psecurity-scan -DskipTests "-Ddependency-check.skip=true" verify
```

## Evidence To Capture For Submission

- Maven `clean verify` output.
- Arquillian Payara profile output.
- OWASP Dependency-Check report.
- CycloneDX SBOM files.
- Payara datasource ping output.
- Dashboard screenshots after seed data loads.
- Screenshots for shipment creation, status update, inventory adjustment, vendor scoring, alert acknowledgement, compliance audit trail, and performance telemetry.
- Any failed test evidence with resolution notes.
