# Cucumber BDD Automation – Auth API

## Overview

This document explains the **Cucumber integration-test framework** added to the
`workplace-tracker-service` project, covering:

- Project structure and file roles
- Dependencies and Gradle tasks
- How the Spring context and PostgreSQL container boot up
- Feature file organisation and scenario catalogue
- Step definition anatomy and shared-state pattern
- How to run and view reports
- How to extend the framework for new API flows

---

## 1. Tech Stack

| Technology | Role |
|---|---|
| **Cucumber 7.15** (`cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine`) | BDD engine – parses `.feature` files and runs step definitions |
| **JUnit 5 Platform Suite** | Drives the Cucumber `@Suite` runner via the standard Gradle `test` or `cucumberTest` tasks |
| **RestAssured 5.4** | HTTP client used in step definitions to call the running Spring Boot server |
| **Testcontainers (PostgreSQL)** | Spins up an isolated, real PostgreSQL database for every test run |
| **Spring Boot Test** (`RANDOM_PORT`) | Starts the full application context on a random port |
| **Liquibase** | Runs all baseline changesets inside the Testcontainer, producing a schema identical to production |

---

## 2. Directory Structure

```
src/
└── test/
    ├── java/com/sid/app/
    │   └── cucumber/
    │       ├── config/
    │       │   └── CucumberSpringConfiguration.java   ← Spring + Testcontainers bootstrap
    │       ├── context/
    │       │   └── ScenarioContext.java               ← Shared state per scenario (@ScenarioScope)
    │       ├── helper/
    │       │   └── TestDataHelper.java                ← JDBC seed / cleanup utilities
    │       ├── hooks/
    │       │   └── CucumberHooks.java                 ← @Before / @After lifecycle hooks
    │       ├── runner/
    │       │   └── AuthApiCucumberRunner.java         ← JUnit 5 Suite entry point
    │       └── steps/
    │           └── AuthApiStepDefinitions.java        ← All step implementations
    └── resources/
        ├── application-cucumber.yaml                 ← Cucumber Spring profile (schema overrides)
        ├── cucumber.properties                        ← Cucumber engine settings
        └── features/
            └── auth/
                └── auth_api.feature                  ← Gherkin scenarios
```

---

## 3. Dependency Setup (`build.gradle` / `gradle.properties`)

### gradle.properties additions
```properties
cucumberVersion=7.15.0
restAssuredVersion=5.4.0
```

### build.gradle additions (already applied)
```groovy
// Cucumber
testImplementation "io.cucumber:cucumber-java:${cucumberVersion}"
testImplementation "io.cucumber:cucumber-spring:${cucumberVersion}"
testImplementation "io.cucumber:cucumber-junit-platform-engine:${cucumberVersion}"

// JUnit Platform Suite
testImplementation "org.junit.platform:junit-platform-suite"
testRuntimeOnly  "org.junit.platform:junit-platform-suite-engine"

// RestAssured
testImplementation "io.rest-assured:rest-assured:${restAssuredVersion}"
```

---

## 4. How It Boots Up (Step-by-Step)

```
./gradlew cucumberTest
         │
         ▼
  JUnit 5 Platform finds AuthApiCucumberRunner (@Suite)
         │
         ▼
  Cucumber engine discovers feature files under features/auth/
         │
         ▼
  CucumberSpringConfiguration (@CucumberContextConfiguration)
    ├── Starts PostgreSQLContainer (one instance for whole run)
    ├── @DynamicPropertySource overrides datasource URL + schema
    └── @SpringBootTest(RANDOM_PORT) starts full application
         │  ├── Liquibase runs all baseline changesets (context=init)
         │  │   → creates tables, seeds user_role & app_subscription
         │  └── EncryptionKeyService.init() seeds encryption_keys row
         ▼
  Cucumber runs each Scenario:
    Before hook  → logs scenario start
    Before(@RequiresSeedData) → TestDataHelper seeds extra DB rows
    Steps execute (Given / When / Then via RestAssured)
    After(@RequiresSeedData)  → TestDataHelper cleans up seed rows
    After hook   → logs result, clears ScenarioContext
```

---

## 5. Spring Profile – `application-cucumber.yaml`

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: public   # override from dev → public (Testcontainers default)
  datasource:
    hikari:
      schema: public
  liquibase:
    contexts: init               # activate Liquibase "init" context for baseline changesets
    default-schema: public
    liquibase-schema: public
```

> **Why?**  The default `application.yaml` uses schema `dev`.  PostgreSQL inside
> Testcontainers only has the `public` schema by default, so all Hibernate and
> Liquibase schema references must be redirected to `public`.

---

## 6. ScenarioContext – Sharing State Between Steps

```
Given a registered and logged-in regular user
  └─ AuthApiStepDefinitions seeds DB, calls Login API,
     stores JWT: scenarioContext.set("jwtToken", token)

When I change the password with currentPassword "..." and newPassword "..."
  └─ Reads JWT:  scenarioContext.get("jwtToken", String.class)
     Sends PATCH /user/change-password with Authorization: Bearer <token>

Then the response HTTP status should be 200
  └─ Asserts lastResponse.statusCode() == 200
```

`ScenarioContext` is a `@ScenarioScope` Spring bean – a **fresh instance is created
for every scenario**, so state never leaks between tests.

---

## 7. Scenario Catalogue

### Section 1 – Registration Validation *(10 scenarios, no DB required)*

| # | Scenario | Expected HTTP |
|---|---|---|
| 1 | Missing `name` field | `400` |
| 2 | Missing `email` field | `400` |
| 3 | Missing `password` field | `400` |
| 4 | Missing `role` field | `400` |
| 5 | Invalid email format | `400` |
| 6 | Password shorter than 8 chars | `400` |
| 7 | Unsupported role value | `400` |
| 8 | SUPER_ADMIN without `platformUserCode` | `400` |
| 9 | SUPER_ADMIN without `tenantCode` | `400` |
| 10 | ADMIN without `tenantUserCode` | `400` |

### Section 2 – Registration Happy Path *(2 scenarios, `@RequiresSeedData`)*

| # | Scenario | Expected HTTP |
|---|---|---|
| 11 | Register SUPER_ADMIN successfully | `200` + JWT token |
| 12 | Register with duplicate email | `400` |

### Section 3 – Login *(3 scenarios)*

| # | Scenario | Expected HTTP |
|---|---|---|
| 13 | Non-existent user | `401` |
| 14 | Valid credentials (`@RequiresSeedData`) | `200` + JWT token |
| 15 | Wrong password (`@RequiresSeedData`) | `401` |

### Section 4 – Refresh Token *(2 scenarios)*

| # | Scenario | Expected HTTP |
|---|---|---|
| 16 | No token provided | `401` |
| 17 | Malformed token | `401` |

### Section 5 – Forgot Password Reset *(1 scenario)*

| # | Scenario | Expected HTTP |
|---|---|---|
| 18 | Invalid OTP | `400` |

### Section 6 – Change Password *(4 scenarios)*

| # | Scenario | Expected HTTP |
|---|---|---|
| 19 | No JWT token | `403` |
| 20 | Wrong current password (`@RequiresSeedData`) | `400` |
| 21 | New password same as current (`@RequiresSeedData`) | `400` |
| 22 | Successful change (`@RequiresSeedData`) | `200` |

**Total: 22 scenarios**

---

## 8. Running the Tests

### Prerequisites

| Requirement | Notes |
|---|---|
| Java 23 | Set `JAVA_HOME` |
| Docker Desktop (or Colima / Podman) | Required for Testcontainers to start PostgreSQL |
| No external PostgreSQL needed | Testcontainers handles it |

### Commands

```bash
# Run ONLY the Cucumber BDD tests
./gradlew cucumberTest

# Run regular unit tests (Cucumber runner excluded)
./gradlew test

# Run everything (unit + BDD)
./gradlew test cucumberTest

# Run a specific tag subset
./gradlew cucumberTest -Dcucumber.filter.tags="@RequiresSeedData"

# Run without seed-data scenarios (validation only – no Docker needed... 
# actually Docker is still needed for the Spring context, but scenarios are simpler)
./gradlew cucumberTest -Dcucumber.filter.tags="not @RequiresSeedData"
```

### Windows (PowerShell)
```powershell
.\gradlew.bat cucumberTest
```

---

## 9. Reports

After a run the following are generated:

| Report | Path |
|---|---|
| HTML (pretty) | `build/reports/cucumber/auth-api-report.html` |
| JSON | `build/reports/cucumber/auth-api-report.json` |
| JUnit XML | `build/reports/cucumber/auth-api-report.xml` |
| Console (pretty) | Printed during the run |

Open the HTML report in a browser:
```bash
open build/reports/cucumber/auth-api-report.html        # macOS
start build/reports/cucumber/auth-api-report.html       # Windows
```

---

## 10. TestDataHelper – Seed Strategy

`@RequiresSeedData` scenarios depend on data that must exist **before** the API call.
Instead of relying on other API calls (which would couple tests), `TestDataHelper`
inserts rows directly via `JdbcTemplate`.

```
seedPlatformUser()
  → platform_user (code=PU-CUC-001)

seedTenant()
  → tenant (code=TNT-CUC-001)

seedAdminTenantUser()
  → calls seedPlatformUser() + seedTenant() first
  → tenant_user (role=ADMIN, code=TU-CUC-ADMIN-01)

seedSuperAdminTenantUser()
  → calls seedPlatformUser() + seedTenant() first
  → tenant_user (role=SUPER_ADMIN)

seedRegularUser()
  → calls seedAdminTenantUser() first
  → users table row (role=USER) — needed for changePassword tests
```

All inserted rows use the email prefix `cuc.` so the cleanup SQL:
```sql
DELETE FROM users        WHERE email LIKE 'cuc.%'
DELETE FROM tenant_user  WHERE email LIKE 'cuc.%'
DELETE FROM platform_user WHERE platform_user_code = 'PU-CUC-001'
DELETE FROM tenant        WHERE tenant_code = 'TNT-CUC-001'
```
is safe to run without affecting other data.

---

## 11. Adding New API Scenarios

### Step 1 – Add Gherkin scenarios
Open (or create) a `.feature` file under `src/test/resources/features/`.

```gherkin
Feature: Office Visit API

  Scenario: Create a visit successfully
    Given a logged-in USER exists
    When I POST a visit for today
    Then the response HTTP status should be 201
```

### Step 2 – Create a new Step Definitions class
```java
package com.sid.app.cucumber.steps;

import io.cucumber.java.en.*;

public class OfficeVisitStepDefinitions {

    @When("I POST a visit for today")
    public void postVisit() { ... }
}
```

Step definition classes **do not need `@RequiredArgsConstructor`** if they inject
Spring beans via the constructor (Cucumber-Spring handles this automatically).

### Step 3 – Create a new runner (optional, for isolation)
```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/office-visit")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.sid.app.cucumber")
public class OfficeVisitCucumberRunner {}
```

### Step 4 – Tag appropriately
- `@RequiresSeedData` → hooks will call `testDataHelper.cleanupSeedData()` after
- `@Ignored` → runner's `FILTER_TAGS_PROPERTY_NAME` excludes these automatically
- Any custom tag for selective execution

---

## 12. Troubleshooting

| Symptom | Likely Cause | Fix |
|---|---|---|
| `Could not find a valid Docker environment` | Docker not running | Start Docker Desktop |
| `Liquibase: relation "user_role" does not exist` | Schema mismatch | Verify `application-cucumber.yaml` sets schema to `public` and `contexts: init` |
| `No step definitions found` | Glue path mismatch | Confirm `@ConfigurationParameter(key=GLUE_PROPERTY_NAME, value="com.sid.app.cucumber")` in runner |
| `ScenarioContext NPE` | Missing `@ScenarioScope` or not injected via constructor | Ensure `ScenarioContext` is `@ScenarioScope` + `@Component` and injected via constructor |
| `Change password returns 404` | Seeded user not in `users` table | Ensure `seedRegularUser()` is called in the `Given` step (not just `seedAdminTenantUser()`) |
| `403 instead of 401 on no-token test` | Spring Security default entry point | Update the `.feature` file scenario expectation to `403`; this is Spring Security's default for stateless configs without an explicit `AuthenticationEntryPoint` |

---

## 13. CI / CD Integration

Add the following step to your pipeline (GitHub Actions example):

```yaml
- name: Run Cucumber BDD Tests
  run: ./gradlew cucumberTest
  env:
    DOCKER_HOST: unix:///var/run/docker.sock   # Testcontainers needs Docker

- name: Publish Cucumber HTML Report
  uses: actions/upload-artifact@v4
  with:
    name: cucumber-auth-report
    path: build/reports/cucumber/
```

For GitLab CI:
```yaml
cucumber-test:
  stage: test
  services:
    - docker:dind
  script:
    - ./gradlew cucumberTest
  artifacts:
    paths:
      - build/reports/cucumber/
    reports:
      junit: build/reports/cucumber/auth-api-report.xml
```

