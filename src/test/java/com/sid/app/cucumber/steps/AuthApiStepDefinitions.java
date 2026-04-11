package com.sid.app.cucumber.steps;

import com.sid.app.cucumber.context.ScenarioContext;
import com.sid.app.cucumber.helper.TestDataHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber step definitions for all Authentication API scenarios defined in
 * {@code features/auth/auth_api.feature}.
 *
 * <h3>Design notes</h3>
 * <ul>
 *   <li>RestAssured targets the local Spring Boot server started on a random port
 *       by {@code @SpringBootTest(webEnvironment = RANDOM_PORT)}.</li>
 *   <li>Each step stores its {@link Response} in {@code lastResponse} (scenario-local field)
 *       so {@code Then} / {@code And} steps can assert against it.</li>
 *   <li>Shared state (JWT token, email) is propagated via {@link ScenarioContext}.</li>
 *   <li>{@link TestDataHelper} handles JDBC seed / cleanup called from
 *       {@link com.sid.app.cucumber.hooks.CucumberHooks}.</li>
 * </ul>
 */
public class AuthApiStepDefinitions {

    private static final Logger log = LoggerFactory.getLogger(AuthApiStepDefinitions.class);

    // ---------------------------------------------------------------
    // Endpoint constants (mirrors EndpointConstants in main source)
    // ---------------------------------------------------------------
    private static final String REGISTER_URL   = "/api/v1/workplace-tracker-service/register";
    private static final String LOGIN_URL      = "/api/v1/workplace-tracker-service/login";
    private static final String REFRESH_URL    = "/api/v1/workplace-tracker-service/auth/refresh";
    private static final String RESET_PWD_URL  = "/api/v1/workplace-tracker-service/forgot/reset";
    private static final String CHANGE_PWD_URL = "/api/v1/workplace-tracker-service/user/change-password";

    // ---------------------------------------------------------------
    // Injected via Cucumber-Spring constructor injection
    // ---------------------------------------------------------------
    @LocalServerPort
    private int port;

    private final ScenarioContext scenarioContext;
    private final TestDataHelper  testDataHelper;

    public AuthApiStepDefinitions(ScenarioContext scenarioContext, TestDataHelper testDataHelper) {
        this.scenarioContext = scenarioContext;
        this.testDataHelper  = testDataHelper;
    }

    /** Holds the last HTTP response so assertions can access it. */
    private Response lastResponse;

    // ================================================================
    // GIVEN – state setup steps
    // ================================================================

    @Given("the prerequisite platform user {string} and tenant {string} exist in the database")
    public void seedPlatformUserAndTenant(String platformUserCode, String tenantCode) {
        testDataHelper.seedPlatformUser();
        testDataHelper.seedTenant();
        log.info("Given: platform_user ({}) and tenant ({}) seeded", platformUserCode, tenantCode);
    }

    @And("a SUPER_ADMIN is already registered with platformUserCode {string} and tenantCode {string}")
    public void registerFirstSuperAdmin(String platformUserCode, String tenantCode) {
        String email = "cuc.first.sa@example.com";
        Map<String, Object> body = buildSuperAdminRequest(email, platformUserCode, tenantCode);
        Response response = doPost(REGISTER_URL, body);
        log.info("Given: first SUPER_ADMIN registered (email={}, status={})", email, response.statusCode());
        scenarioContext.set(ScenarioContext.LAST_REGISTERED_EMAIL, email);
    }

    @Given("a seeded tenant user with email {string} and password {string} exists")
    public void seedTenantUserDirectly(String email, String ignoredPassword) {
        // Seeds platform_user + tenant + SUPER_ADMIN tenant_user via JDBC.
        // 'ignoredPassword' comes from the feature file for readability; the actual
        // encrypted password is set by TestDataHelper using TEST_PASSWORD constant.
        testDataHelper.seedSuperAdminTenantUser();
        log.info("Given: super_admin tenant_user seeded (email={})", email);
    }

    @Given("a registered and logged-in regular user")
    public void setupRegisteredAndLoggedInRegularUser() {
        // 1. Seed prerequisite rows via JDBC
        testDataHelper.seedRegularUser();

        // 2. Login as the regular USER to obtain a JWT
        Map<String, Object> loginBody = new HashMap<>();
        loginBody.put("email",    TestDataHelper.REGULAR_USER_EMAIL);
        loginBody.put("password", TestDataHelper.TEST_PASSWORD);

        Response loginResponse = doPost(LOGIN_URL, loginBody);

        assertThat(loginResponse.statusCode())
                .as("Regular user login during Given setup should return 200")
                .isEqualTo(200);

        String token = loginResponse.jsonPath().getString("token");
        assertThat(token)
                .as("JWT token must be present after login in Given setup")
                .isNotBlank();

        scenarioContext.set(ScenarioContext.JWT_TOKEN,            token);
        scenarioContext.set(ScenarioContext.REGISTERED_EMAIL,     TestDataHelper.REGULAR_USER_EMAIL);
        scenarioContext.set(ScenarioContext.REGISTERED_PASSWORD,  TestDataHelper.TEST_PASSWORD);

        log.info("Given: regular user logged in, JWT stored in ScenarioContext");
    }

    // ================================================================
    // WHEN – Registration
    // ================================================================

    @When("I submit a register request with missing {string} field")
    public void submitRegisterWithMissingField(String missingField) {
        Map<String, Object> body = buildMinimalValidUserRequest();
        body.remove(missingField);
        lastResponse = doPost(REGISTER_URL, body);
    }

    @When("I submit a register request with email {string} role {string} and tenantUserCode {string}")
    public void submitRegisterWithCustomEmail(String email, String role, String tenantUserCode) {
        Map<String, Object> body = buildMinimalValidUserRequest();
        body.put("email",          email);
        body.put("role",           role);
        body.put("tenantUserCode", tenantUserCode);
        lastResponse = doPost(REGISTER_URL, body);
    }

    @When("I submit a register request with password {string} role {string} and tenantUserCode {string}")
    public void submitRegisterWithCustomPassword(String password, String role, String tenantUserCode) {
        Map<String, Object> body = buildMinimalValidUserRequest();
        body.put("password",       password);
        body.put("role",           role);
        body.put("tenantUserCode", tenantUserCode);
        lastResponse = doPost(REGISTER_URL, body);
    }

    @When("I submit a register request with role {string}")
    public void submitRegisterWithCustomRole(String role) {
        Map<String, Object> body = buildMinimalValidUserRequest();
        body.put("role", role);
        lastResponse = doPost(REGISTER_URL, body);
    }

    @When("I submit a SUPER_ADMIN register request without platformUserCode")
    public void submitSuperAdminWithoutPlatformUserCode() {
        lastResponse = doPost(REGISTER_URL,
                buildSuperAdminRequest("cuc.nopuc@example.com", null, "TNT-DUMMY"));
    }

    @When("I submit a SUPER_ADMIN register request without tenantCode")
    public void submitSuperAdminWithoutTenantCode() {
        lastResponse = doPost(REGISTER_URL,
                buildSuperAdminRequest("cuc.notc@example.com", "PU-DUMMY", null));
    }

    @When("I submit an ADMIN register request without tenantUserCode")
    public void submitAdminWithoutTenantUserCode() {
        Map<String, Object> body = new HashMap<>();
        body.put("name",     "Cuc Admin");
        body.put("email",    "cuc.notuc@example.com");
        body.put("mobileNumber", "+9990040004");
        body.put("password", TestDataHelper.TEST_PASSWORD);
        body.put("role",     "ADMIN");
        // tenantUserCode intentionally omitted
        lastResponse = doPost(REGISTER_URL, body);
    }

    @When("I register a SUPER_ADMIN with platformUserCode {string} and tenantCode {string}")
    public void registerSuperAdmin(String platformUserCode, String tenantCode) {
        String email = "cuc.superadmin@example.com";
        lastResponse = doPost(REGISTER_URL, buildSuperAdminRequest(email, platformUserCode, tenantCode));
        scenarioContext.set(ScenarioContext.REGISTERED_EMAIL,    email);
        scenarioContext.set(ScenarioContext.REGISTERED_PASSWORD, TestDataHelper.TEST_PASSWORD);
    }

    @When("I register another SUPER_ADMIN with the same email")
    public void registerSuperAdminDuplicate() {
        String email = (String) scenarioContext.get(ScenarioContext.LAST_REGISTERED_EMAIL);
        lastResponse = doPost(REGISTER_URL,
                buildSuperAdminRequest(email,
                        TestDataHelper.PLATFORM_USER_CODE,
                        TestDataHelper.TENANT_CODE));
    }

    // ================================================================
    // WHEN – Login
    // ================================================================

    @When("I login with email {string} and password {string}")
    public void login(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email",    email);
        body.put("password", password);
        lastResponse = doPost(LOGIN_URL, body);
    }

    // ================================================================
    // WHEN – Refresh Token
    // ================================================================

    @When("I request a token refresh without any token")
    public void refreshWithoutToken() {
        lastResponse = RestAssured
                .given()
                    .baseUri(baseUri())
                    .contentType(ContentType.JSON)
                .when()
                    .post(REFRESH_URL);
    }

    @When("I request a token refresh with bearer token {string}")
    public void refreshWithToken(String token) {
        lastResponse = RestAssured
                .given()
                    .baseUri(baseUri())
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                .when()
                    .post(REFRESH_URL);
    }

    // ================================================================
    // WHEN – Reset Password
    // ================================================================

    @When("I request a password reset for email {string} with OTP {string} and newPassword {string}")
    public void requestPasswordReset(String email, String otp, String newPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("email",       email);
        body.put("otp",         otp);
        body.put("newPassword", newPassword);
        lastResponse = doPost(RESET_PWD_URL, body);
    }

    // ================================================================
    // WHEN – Change Password
    // ================================================================

    @When("I send a change password request without a JWT token")
    public void changePasswordWithoutJwt() {
        Map<String, Object> body = new HashMap<>();
        body.put("currentPassword", "OldPass123!");
        body.put("newPassword",     "NewPass456!");

        lastResponse = RestAssured
                .given()
                    .baseUri(baseUri())
                    .contentType(ContentType.JSON)
                    .body(body)
                .when()
                    .patch(CHANGE_PWD_URL);
    }

    @When("I change the password with currentPassword {string} and newPassword {string}")
    public void changePassword(String currentPassword, String newPassword) {
        String token = scenarioContext.get(ScenarioContext.JWT_TOKEN, String.class);

        Map<String, Object> body = new HashMap<>();
        body.put("currentPassword", currentPassword);
        body.put("newPassword",     newPassword);

        lastResponse = RestAssured
                .given()
                    .baseUri(baseUri())
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(body)
                .when()
                    .patch(CHANGE_PWD_URL);
    }

    // ================================================================
    // THEN / AND – Assertions
    // ================================================================

    @Then("the response HTTP status should be {int}")
    public void verifyHttpStatus(int expectedStatus) {
        assertThat(lastResponse.statusCode())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expectedStatus, lastResponse.statusCode(), lastResponse.body().asString())
                .isEqualTo(expectedStatus);
    }

    @And("the response error message should contain {string}")
    public void verifyErrorMessage(String expectedFragment) {
        String body = lastResponse.body().asString();
        assertThat(body)
                .as("Body should contain \"%s\". Actual: %s", expectedFragment, body)
                .containsIgnoringCase(expectedFragment);
    }

    @And("the response status field should be {string}")
    public void verifyStatusField(String expectedStatus) {
        String actual = lastResponse.jsonPath().getString("status");
        assertThat(actual)
                .as("JSON 'status' field")
                .isEqualTo(expectedStatus);
    }

    @And("the response should contain a valid JWT token")
    public void verifyJwtTokenPresent() {
        String token = lastResponse.jsonPath().getString("token");
        assertThat(token)
                .as("Response should carry a non-blank JWT token")
                .isNotBlank();
        assertThat(token.split("\\."))
                .as("JWT must have 3 dot-separated segments")
                .hasSize(3);
    }

    @And("the response message should be {string}")
    public void verifyResponseMessage(String expectedMessage) {
        String body = lastResponse.body().asString();
        assertThat(body)
                .as("Body should contain message \"%s\". Actual: %s", expectedMessage, body)
                .containsIgnoringCase(expectedMessage);
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private Map<String, Object> buildMinimalValidUserRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("name",           "Cucumber Tester");
        body.put("email",          "cuc.tester@example.com");
        body.put("mobileNumber",   "+9990020002");
        body.put("password",       "ValidPass123!");
        body.put("role",           "USER");
        body.put("tenantUserCode", "TU-DUMMY");
        return body;
    }

    private Map<String, Object> buildSuperAdminRequest(
            String email, String platformUserCode, String tenantCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("name",         "Cuc Super Admin");
        body.put("email",        email);
        body.put("mobileNumber", "+9990030003");
        body.put("password",     TestDataHelper.TEST_PASSWORD);
        body.put("role",         "SUPER_ADMIN");
        if (platformUserCode != null) body.put("platformUserCode", platformUserCode);
        if (tenantCode        != null) body.put("tenantCode",       tenantCode);
        return body;
    }

    private Response doPost(String path, Map<String, Object> body) {
        return RestAssured
                .given()
                    .baseUri(baseUri())
                    .contentType(ContentType.JSON)
                    .body(body)
                .when()
                    .post(path);
    }

    private String baseUri() {
        return "http://localhost:" + port;
    }
}

