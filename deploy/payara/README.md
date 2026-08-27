# Payara Server 6 Deployment

This project targets Payara Server 6, Jakarta EE 10, Java 17, EAR packaging, and MySQL.

## Prerequisites

- Payara Server 6 running on Java 17
- MySQL 8
- MySQL Connector/J copied into `${PAYARA_HOME}/glassfish/domains/domain1/lib`
- Database created with `deploy/mysql/schema.sql`

## Configure Payara

Run these commands from `${PAYARA_HOME}/bin` after starting the domain:

```bash
asadmin start-domain
asadmin multimode --file deploy/payara/setup-domain.asadmin
asadmin ping-connection-pool globaltradePool
```

When `create-file-user` asks for passwords, use strong passwords for the configured operational users: `admin`, `coordinator`, `warehouse`, and `customs`.

## Deploy

```bash
mvn clean verify
asadmin deploy --force=true globaltrade-logistics-ear/target/globaltrade-logistics.ear
```

Open `http://localhost:8080/globaltrade` and sign in with one of the configured Payara file-realm users.
