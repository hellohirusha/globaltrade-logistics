package com.hiru.globaltrade.security.jaas;

import com.sun.appserv.security.AppservPasswordLoginModule;
import com.sun.enterprise.security.auth.realm.Realm;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SupplyChainLoginModule extends AppservPasswordLoginModule {
    private static final Logger LOGGER = Logger.getLogger(SupplyChainLoginModule.class.getName());
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "globaltrade_logistics";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USER_TABLE = "auth_users";
    private static final String DEFAULT_ROLE_TABLE = "auth_user_roles";

    @Override
    protected void authenticateUser() throws LoginException {
        String username = getUsername();
        char[] password = getPasswordChar();
        if (isBlank(username) || password == null || password.length == 0) {
            throw new FailedLoginException("Username and password are required.");
        }

        try (Connection connection = openConnection()) {
            AuthenticatedUser user = loadUser(connection, username);
            if (user == null) {
                audit(connection, username, "LOGIN", "FAILED", "Unknown supply-chain user.");
                throw new FailedLoginException("Invalid username or password.");
            }
            if (!user.active()) {
                audit(connection, username, "LOGIN", "FAILED", "Inactive supply-chain user.");
                throw new FailedLoginException("User account is inactive.");
            }
            if (user.locked()) {
                audit(connection, username, "LOGIN", "FAILED", "Locked supply-chain user.");
                throw new FailedLoginException("User account is locked.");
            }
            if (user.passwordExpired()) {
                audit(connection, username, "LOGIN", "FAILED", "Expired supply-chain credential.");
                throw new FailedLoginException("User password is expired.");
            }
            if (!PasswordHashing.matches(password, user.passwordSalt(), user.passwordIterations(), user.passwordHash())) {
                recordFailedAttempt(connection, username);
                audit(connection, username, "LOGIN", "FAILED", "Invalid supply-chain credential.");
                throw new FailedLoginException("Invalid username or password.");
            }
            if (user.roles().isEmpty()) {
                audit(connection, username, "LOGIN", "FAILED", "Supply-chain user has no mapped roles.");
                throw new FailedLoginException("User has no assigned security roles.");
            }

            recordSuccessfulLogin(connection, username);
            audit(connection, username, "LOGIN", "SUCCESS", "Supply-chain authentication accepted.");
            commitUserAuthentication(user.roles().toArray(String[]::new));
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "Supply-chain authentication failed because the auth store could not be read.", ex);
            LoginException loginException = new LoginException("Authentication service is unavailable.");
            loginException.initCause(ex);
            throw loginException;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private AuthenticatedUser loadUser(Connection connection, String username) throws SQLException {
        String sql = "select password_hash, password_salt, password_iterations, active, account_locked, password_expires_at "
                + "from " + table("auth.user.table", DEFAULT_USER_TABLE) + " where username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Timestamp expiry = resultSet.getTimestamp("password_expires_at");
                return new AuthenticatedUser(
                        resultSet.getString("password_hash"),
                        resultSet.getString("password_salt"),
                        resultSet.getInt("password_iterations"),
                        resultSet.getBoolean("active"),
                        resultSet.getBoolean("account_locked"),
                        expiry != null && expiry.toInstant().isBefore(Instant.now()),
                        loadRoles(connection, username)
                );
            }
        }
    }

    private List<String> loadRoles(Connection connection, String username) throws SQLException {
        String sql = "select role_name from " + table("auth.role.table", DEFAULT_ROLE_TABLE) + " where username = ?";
        List<String> roles = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    roles.add(resultSet.getString("role_name"));
                }
            }
        }
        return roles;
    }

    private void recordSuccessfulLogin(Connection connection, String username) throws SQLException {
        String sql = "update " + table("auth.user.table", DEFAULT_USER_TABLE)
                + " set failed_attempts = 0, last_login_at = current_timestamp(6), updated_at = current_timestamp(6) where username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    private void recordFailedAttempt(Connection connection, String username) throws SQLException {
        String sql = "update " + table("auth.user.table", DEFAULT_USER_TABLE)
                + " set failed_attempts = failed_attempts + 1, account_locked = failed_attempts + 1 >= 5, updated_at = current_timestamp(6) where username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    private void audit(Connection connection, String username, String eventType, String outcome, String message) throws SQLException {
        String sql = "insert into login_audit (username, event_type, outcome, source_ip, user_agent, message, created_at) "
                + "values (?, ?, ?, ?, ?, ?, current_timestamp(6))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, eventType);
            statement.setString(3, outcome);
            statement.setString(4, config("auth.source.ip", "payara-jaas"));
            statement.setString(5, config("auth.user.agent", "Payara JAAS"));
            statement.setString(6, message);
            statement.executeUpdate();
        }
    }

    private Connection openConnection() throws SQLException {
        String datasourceJndi = config("datasource-jndi", "");
        if (!isBlank(datasourceJndi)) {
            return openDataSourceConnection(datasourceJndi);
        }

        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("user", required("auth.db.user"));
        connectionProperties.setProperty("password", required("auth.db.password"));
        return DriverManager.getConnection(jdbcUrl(), connectionProperties);
    }

    private Connection openDataSourceConnection(String datasourceJndi) throws SQLException {
        try {
            Object resource = new InitialContext().lookup(datasourceJndi);
            if (!(resource instanceof DataSource dataSource)) {
                throw new SQLException("JAAS datasource is not a javax.sql.DataSource: " + datasourceJndi);
            }
            return dataSource.getConnection();
        } catch (NamingException ex) {
            throw new SQLException("Unable to resolve JAAS datasource: " + datasourceJndi, ex);
        }
    }

    private String jdbcUrl() {
        String explicitUrl = config("auth.db.url", "");
        if (!isBlank(explicitUrl)) {
            return explicitUrl;
        }
        String host = config("auth.db.host", DEFAULT_HOST);
        String port = config("auth.db.port", DEFAULT_PORT);
        String database = config("auth.db.name", DEFAULT_DATABASE);
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private String table(String property, String fallback) {
        return validateIdentifier(config(property, fallback));
    }

    private String required(String property) {
        String value = config(property, "");
        if (isBlank(value)) {
            throw new IllegalStateException("Missing JAAS realm property: " + property);
        }
        return value;
    }

    private String config(String property, String fallback) {
        String value = fromRealm(property);
        if (!isBlank(value)) {
            return value;
        }
        Object option = _options == null ? null : _options.get(property);
        if (option != null && !isBlank(option.toString())) {
            return option.toString();
        }
        return Objects.toString(System.getProperty("globaltrade." + property), fallback);
    }

    private String fromRealm(String property) {
        Realm realm = getCurrentRealm();
        return realm == null ? null : realm.getProperty(property);
    }

    private String validateIdentifier(String identifier) {
        if (!identifier.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Unsafe database identifier in JAAS realm configuration.");
        }
        return identifier;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record AuthenticatedUser(
            String passwordHash,
            String passwordSalt,
            int passwordIterations,
            boolean active,
            boolean locked,
            boolean passwordExpired,
            List<String> roles
    ) {
    }
}
