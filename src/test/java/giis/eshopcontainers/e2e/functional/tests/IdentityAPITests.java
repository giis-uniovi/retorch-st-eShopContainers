package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.retorch.annotations.AccessMode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * The {@code IdentityAPITests} validates the Identity API (OIDC / IdentityServer4) endpoints.
 * Each test first attempts the request through the Desktop BFF gateway base URL with the
 * {@code /identity-api} prefix; if the gateway does not have an Identity route configured,
 * the test falls back to the Identity service URL directly so that the contract is still
 * exercised and not silently skipped.
 *
 * <p>In the eShopOnContainers architecture the Identity service is intentionally NOT routed
 * through the BFF gateway — clients must obtain tokens before issuing any gateway request,
 * so making the token issuer reachable via the gateway would be a bootstrap loop. These tests
 * therefore document the dual-path reality and exercise the endpoints through whichever path
 * is reachable.
 *
 * <p>Endpoints under test:
 * <ul>
 *   <li>GET /.well-known/openid-configuration — OIDC discovery document</li>
 *   <li>GET /connect/userinfo                 — OIDC userinfo (claims for the bearer)</li>
 *   <li>POST /connect/token                   — token endpoint (covered indirectly via {@link BaseAPIClass#getTokenForScope})</li>
 * </ul>
 */
class IdentityAPITests extends BaseAPIClass {

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testGetOpenIdConfigurationIdentityAPI")
    void testGetOpenIdConfiguration() throws IOException {
        // Standard OIDC discovery document — must include issuer and key URIs.
        String result = getIdentityBody("/.well-known/openid-configuration", null);
        Assertions.assertFalse(result.isEmpty(), "OpenID configuration response must not be empty");
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        Assertions.assertTrue(json.has("issuer"), "Discovery doc must contain 'issuer'");
        Assertions.assertTrue(json.has("token_endpoint"), "Discovery doc must contain 'token_endpoint'");
        Assertions.assertTrue(json.has("jwks_uri"), "Discovery doc must contain 'jwks_uri'");
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READONLY")
    @Test
    @DisplayName("testGetCurrentUserInfoIdentityAPI")
    void testGetCurrentUserInfo() throws IOException {
        // Userinfo requires a token issued with the openid (and ideally profile) scope.
        String openIdToken = getTokenForScope(getIdentityURL(), "openid profile");
        String result = getIdentityBody("/connect/userinfo", openIdToken);
        Assertions.assertFalse(result.isEmpty(), "Userinfo response must not be empty");
        JsonObject userInfo = JsonParser.parseString(result).getAsJsonObject();
        Assertions.assertTrue(userInfo.has("sub"), "Userinfo must contain 'sub' claim");
        Assertions.assertFalse(userInfo.get("sub").getAsString().isEmpty(), "User 'sub' claim must not be empty");
        Assertions.assertEquals(getUser(), userInfo.get("preferred_username").getAsString(),
                "Userinfo 'preferred_username' must match the test user");
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READONLY")
    @Test
    @DisplayName("testGetTokenWithValidCredentialsIdentityAPI")
    void testGetTokenWithValidCredentials() throws IOException {
        // POST /connect/token with the Resource Owner Password Credentials flow must return a
        // non-empty access token for the basket scope (the same scope used in the global setup).
        String token = getTokenForScope(getIdentityURL(), "basket");
        Assertions.assertNotNull(token, "Token must not be null");
        Assertions.assertFalse(token.isEmpty(), "Token must not be empty");
        // JWT tokens have three Base64URL segments separated by '.'
        Assertions.assertEquals(3, token.split("\\.").length, "Access token must be a JWT with 3 segments");
    }

    // -----------------------------------------------------------------------
    // Helpers — try the gateway first, fall back to direct identity URL
    // -----------------------------------------------------------------------

    /**
     * Issues a GET against the given identity-relative path. Tries the BFF gateway with the
     * {@code /identity-api} prefix first; if the gateway returns 404 (no proxy route), the request
     * is retried against the identity service URL directly so the endpoint is still exercised.
     *
     * @param path  identity-relative path including leading slash (e.g. {@code "/connect/userinfo"})
     * @param token optional Bearer token; pass {@code null} for unauthenticated calls
     * @return response body, or empty string if both attempts fail
     */
    private String getIdentityBody(String path, String token) throws IOException {
        // 1) Try the gateway path prefix
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.getDesktopBFFIdentityURL() + path);
            if (token != null) request.addHeader("Authorization", "Bearer " + token);
            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : "";
            }
            log.debug("Identity path {} not exposed by the gateway (status {}), falling back to direct URL",
                    path, status);
        }
        // 2) Fall back to the Identity service URL directly
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(getIdentityURL() + path);
            if (token != null) request.addHeader("Authorization", "Bearer " + token);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : "";
        }
    }
}
