# GlobalTrade Logistics - Payara 6 and MySQL Deployment Guide

This guide deploys GlobalTrade Logistics as a Jakarta EE 10 EAR on Payara Server 6 with MySQL.

## 1. Prerequisites

- Java 17
- Payara Server 6 installed at `<path-to-payara6>` (e.g. `C:\Payara\payara6` on Windows, `/opt/payara6` on Linux/macOS)
- MySQL 8
- Maven 3.9, or the Maven distribution bundled with your IDE
- MySQL Connector/J copied into `<PAYARA_HOME>/glassfish/domains/domain1/lib`

## 2. Create Database and Local Database User

Create the application user with a local password that is not committed to the repository. Use the same password later
for the Payara password alias `globaltrade.db.password`:

```sql
CREATE USER IF NOT EXISTS 'globaltrade_app'@'localhost' IDENTIFIED BY '<your-local-db-password>';
CREATE USER IF NOT EXISTS 'globaltrade_app'@'%' IDENTIFIED BY '<your-local-db-password>';
ALTER USER 'globaltrade_app'@'localhost' IDENTIFIED BY '<your-local-db-password>';
ALTER USER 'globaltrade_app'@'%' IDENTIFIED BY '<your-local-db-password>';
GRANT ALL PRIVILEGES ON globaltrade_logistics.* TO 'globaltrade_app'@'localhost';
GRANT ALL PRIVILEGES ON globaltrade_logistics.* TO 'globaltrade_app'@'%';
FLUSH PRIVILEGES;
```

Apply the schema:

```bash
mysql -u root -p < deploy/mysql/schema.sql
```

The schema creates the database `globaltrade_logistics`, authentication and role membership tables, login and security
audit tables, the logistics operation tables for vendors, shipments, inventory, alerts, compliance audit, and
performance metrics, and a deployment audit table.

## 3. Build the Security Extension

Payara must load the custom JAAS login module from the domain library before authentication can work:

```powershell
mvn -pl globaltrade-logistics-security package
.\deploy\payara\install-security-extension.ps1 -PayaraHome <path-to-payara6> -DomainName domain1
```

Restart Payara:

```bash
asadmin restart-domain domain1
```

## 4. Configure Datasource and Realm

Create the password alias with the same local password used by `globaltrade_app`:

```bash
asadmin start-domain
asadmin create-password-alias globaltrade.db.password
```

Run the domain setup script (from the project root, or substitute the full path to the file on your machine):

```bash
asadmin multimode --file deploy/payara/setup-domain.asadmin
asadmin ping-connection-pool globaltradePool
```

The tracked setup file references `${ALIAS=globaltrade.db.password}`. No cleartext database password is stored in source
control.

If `multimode` reports that `globaltradeRealm` already exists, the realm has already been created. Confirm its
datasource with:

```bash
asadmin get server-config.security-service.auth-realm.globaltradeRealm.*
```

If `ping-connection-pool globaltradePool` fails with an access-denied error for `globaltrade_app`@`localhost`, update
the MySQL `localhost` user's password and then run `asadmin update-password-alias globaltrade.db.password` with the same
password.

If you need to reconfigure the realm and datasource from scratch (for example, after changing the database password),
remove the existing resources before rerunning the setup script:

```bash
asadmin delete-auth-realm globaltradeRealm
asadmin delete-jdbc-resource jdbc/globaltradeDS
asadmin delete-jdbc-connection-pool globaltradePool
asadmin create-password-alias globaltrade.db.password
asadmin multimode --file deploy/payara/setup-domain.asadmin
asadmin ping-connection-pool globaltradePool
```

## 5. Build and Deploy the EAR

```bash
mvn clean verify
asadmin deploy --force=true globaltrade-logistics-ear/target/globaltrade-logistics.ear
```

Open:

```text
http://localhost:8080/globaltrade
```

Sign in with one of the MySQL-backed operational users seeded by the schema (see `deploy/mysql/schema.sql` for the
seeded roles). Rotate bootstrap passwords before using this in a shared environment.

## 6. Production Hardening Checklist

- Rotate bootstrap user passwords before any shared use.
- Keep the MySQL app password only in MySQL and in the Payara password alias - never in source control.
- Run the full `security-scan` Maven profile with a valid NVD API key before release evidence capture.
- Keep Payara, MySQL Connector/J, and Java patched.
- Back up `globaltrade_logistics` before schema changes.
- Review the audit, security, and performance-metric tables during testing.
- Enable HTTPS on Payara before exposing the system outside `localhost`.

## 7. Test Evidence To Collect

Use the root Maven build for unit tests, packaging, Checkstyle, and module verification. Use the Arquillian profile only
after Payara, MySQL, the datasource, and the custom JAAS realm are configured:

```bash
mvn clean verify
mvn -Parquillian-payara -pl globaltrade-logistics-ear -am "-Dglobaltrade.it.password=<local-test-password>" verify
```

JMeter plans in `jmeter/` target the deployed context root `/globaltrade` and REST API base path `/api`. Override
runtime settings with `-Jhost`, `-Jport`, `-JcontextRoot`, `-Jusername`, `-Jpassword`, `-Jthreads`, `-Jloops`, and
`-Jramp`.
