package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 The IdentityAPITests suite validates the Identity API (OIDC/IdentityServer4) endpoints
 through the two routes of eShopOnContainers.
 Due to the purpose of provide the access tokens before any other request, the test check
 first the /identity-api prefix, and fall back to the identity URI if is not configured.
 *
 * <p>Endpoints under test:
 * <ul>
 *   <li>GET /.well-known/openid-configuration — OIDC discovery document</li>
 *   <li>GET /connect/userinfo                 — OIDC userinfo (claims for the bearer)</li>
 *   <li>POST /connect/token                   — token endpoint (covered indirectly via {@link BaseAPIClass#getTokenForScope})</li>
 * </ul>
 */
class IdentityAPITests extends BaseAPIClass {

    /**Validates the standard OIDC discovery tokens with the issuer and key URIs*/
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
        Assertions.assertEquals(getUser(), userInfo.get("preferred_username").getAsString(), "Userinfo 'preferred_username' must match the test user");
    }

    /** Tests the /connect/token endpoint with the owner password credentials, checking that must return
     * a no-empty access token for the basket scope */
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READONLY")
    @Test
    @DisplayName("testGetTokenWithValidCredentialsIdentityAPI")
    void testGetTokenWithValidCredentials() throws IOException {
        String token = getTokenForScope(getIdentityURL(), "basket");
        Assertions.assertNotNull(token, "Token must not be null");
        Assertions.assertFalse(token.isEmpty(), "Token must not be empty");
        // JWT tokens have three Base64URL segments separated by '.'
        Assertions.assertEquals(3, token.split("\\.").length, "Access token must be a JWT with 3 segments");
    }
}
