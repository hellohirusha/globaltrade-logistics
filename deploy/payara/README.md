# Payara Server 6 Deployment

This project targets Payara Server 6, Jakarta EE 10, Java 17, EAR packaging, and MySQL.

## Prerequisites

- Payara Server 6 running on Java 17
- MySQL 8
- MySQL Connector/J copied into `${PAYARA_HOME}/glassfish/domains/domain1/lib`
- Database created with `deploy/mysql/schema.sql`
- MySQL user `globaltrade_app` created with a local password
- GlobalTrade security extension installed into `${PAYARA_HOME}/glassfish/domains/domain1/lib`

## Install Security Extension

Run from the project root:

```powershell
mvn -pl globaltrade-logistics-security package
.\deploy\payara\install-security-extension.ps1 -PayaraHome C:\Payara\payara6 -DomainName domain1
```

Restart Payara after installing the extension:

```bash
asadmin restart-domain domain1
```

## Configure Payara

Create a Payara password alias before running the domain setup. Use the same local password that was assigned to the MySQL `globaltrade_app` user:

```bash
asadmin start-domain
asadmin create-password-alias globaltrade.db.password
```

Run the setup script from `${PAYARA_HOME}/bin`:

```bash
asadmin multimode --file C:/Users/neth/Documents/Projects/intelli_j-idea-projects/bcd-ii-final-project/globaltrade-logistics/deploy/payara/setup-domain.asadmin
asadmin ping-connection-pool globaltradePool
```

The setup script references `${ALIAS=globaltrade.db.password}` for the JDBC pool. The custom JAAS module uses `jdbc/globaltradeDS`, so the realm does not need its own database password. The real database password is stored encrypted in the Payara domain configuration, not in the repository.

The realm `globaltradeRealm` authenticates through the custom supply-chain JAAS login module and reads users from MySQL. Bootstrap users are stored in `auth_users` and `auth_user_roles`; rotate bootstrap passwords before using a shared environment.

## Deploy

```bash
mvn clean verify
asadmin deploy --force=true globaltrade-logistics-ear/target/globaltrade-logistics.ear
```

Open `http://localhost:8080/globaltrade` and sign in with one of the configured MySQL-backed operational users.
