package com.hiru.globaltrade.ear;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ArquillianExtension.class)
class GlobalTradeSecurityArquillianIT {
    @ArquillianResource
    private URL baseUrl;

    @Deployment(testable = false)
    public static EnterpriseArchive deployment() {
        return ShrinkWrap.createFromZipFile(EnterpriseArchive.class, new java.io.File("target/globaltrade-logistics.ear"));
    }

    @Test
    void protectedDashboardRejectsAnonymousTraffic() throws Exception {
        Response response = get("api/dashboard", null, null);

        assertThat(response.status()).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void adminCanReadAllLiveOperationSections() throws Exception {
        Credentials admin = credentials("admin");

        assertThat(get("api/dashboard", admin).status()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(get("api/shipments", admin).status()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(get("api/inventory", admin).status()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(get("api/vendors", admin).status()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(get("api/compliance?limit=5", admin).status()).isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void warehouseManagerCannotReadSupplierGovernanceApi() throws Exception {
        Response response = get("api/vendors", credentials("warehouse"));

        assertThat(response.status()).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
        assertThat(response.body()).contains("Access denied");
    }

    @Test
    void coordinatorCannotReadCustomsAuditApi() throws Exception {
        Response response = get("api/compliance?limit=5", credentials("coordinator"));

        assertThat(response.status()).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
        assertThat(response.body()).contains("Access denied");
    }

    @Test
    void invalidShipmentRouteReturnsBusinessRuleFailure() throws Exception {
        String payload = """
                {
                  "reference": "GTL-ARQ-ROUTE",
                  "origin": "Colombo",
                  "destination": "colombo",
                  "carrier": "ContainerLink",
                  "vendorCode": "VEN-SG-001",
                  "priority": "STANDARD",
                  "customsReference": "CUS-ARQ-001",
                  "estimatedDelivery": "2099-01-01T12:00:00"
                }
                """;

        Response response = post("api/shipments", credentials("coordinator"), payload);

        assertThat(response.status()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        assertThat(response.body()).contains("Origin and destination must be different");
    }

    @Test
    void duplicateVendorCodeReturnsBusinessRuleFailure() throws Exception {
        String payload = """
                {
                  "vendorCode": "VEN-SG-001",
                  "name": "Duplicate Vendor",
                  "country": "Singapore",
                  "score": 90,
                  "active": true
                }
                """;

        Response response = post("api/vendors", credentials("admin"), payload);

        assertThat(response.status()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        assertThat(response.body()).contains("Vendor code already exists");
    }

    private Response get(String path, Credentials credentials) throws IOException {
        return get(path, credentials == null ? null : credentials.username(), credentials == null ? null : credentials.password());
    }

    private Response get(String path, String username, String password) throws IOException {
        HttpURLConnection connection = open(path, "GET", username, password);
        return response(connection);
    }

    private Response post(String path, Credentials credentials, String body) throws IOException {
        HttpURLConnection connection = open(path, "POST", credentials.username(), credentials.password());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return response(connection);
    }

    private HttpURLConnection open(String path, String method, String username, String password) throws IOException {
        URL url = new URL(baseUrl, path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");
        if (username != null && password != null) {
            String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + token);
        }
        return connection;
    }

    private Response response(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        byte[] bytes = status >= 400 && connection.getErrorStream() != null
                ? connection.getErrorStream().readAllBytes()
                : connection.getInputStream().readAllBytes();
        return new Response(status, new String(bytes, StandardCharsets.UTF_8));
    }

    private Credentials credentials(String role) {
        return new Credentials(
                System.getProperty("globaltrade.it." + role + "User"),
                System.getProperty("globaltrade.it." + role + "Password")
        );
    }

    private record Credentials(String username, String password) {
    }

    private record Response(int status, String body) {
    }
}
