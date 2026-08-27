# Testing Strategy

## Scope

The test approach follows the assignment requirement for timer services, interceptors, transaction behavior, security validation, performance evidence, exception handling, and deployment verification.

## Automated Tests Included

- Common module role contract test verifies enterprise role constants.
- EJB risk scoring tests validate high-risk and low-risk logistics scenarios.
- EJB annotation tests verify stateless service and timer callback presence.
- Web asset test verifies that the dashboard contains operational sections for shipments, inventory, vendors, compliance, and telemetry.

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

- Payara file realm authentication.
- Group to role mappings in `glassfish-web.xml`.
- Web constraint on all dashboard and API paths.
- EJB method-level role restrictions.
- Compliance audit records for command methods.

## Evidence To Capture For Submission

- Maven `clean verify` output.
- Payara datasource ping output.
- Dashboard screenshots after seed data loads.
- Screenshots for shipment creation, status update, inventory adjustment, vendor scoring, alert acknowledgement, compliance audit trail, and performance telemetry.
- Any failed test evidence with resolution notes.
