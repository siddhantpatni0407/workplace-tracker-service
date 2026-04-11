package com.sid.app.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.DockerClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps the full Spring application context for Cucumber integration tests.
 *
 * <p>A single PostgreSQL Testcontainer is started once per test run (static field).
 * {@link DynamicPropertySource} wires the container's JDBC URL, username and password
 * into the Spring context, overriding the defaults from {@code application.yaml}.
 * The {@code cucumber} Spring profile activates {@code application-cucumber.yaml}
 * which ensures Liquibase runs under the {@code public} schema instead of {@code dev}.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cucumber")
public class CucumberSpringConfiguration {

    // One container reused for the entire test run
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("workplace_tracker_test")
                    .withUsername("cuc_user")
                    .withPassword("cuc_pass");

    private static final Logger LOG = LoggerFactory.getLogger(CucumberSpringConfiguration.class);

    static {
        try {
            // Check whether a Docker environment is available before attempting to start the container.
            if (DockerClientFactory.instance().isDockerAvailable()) {
                POSTGRES.start();
                LOG.info("Testcontainers PostgreSQL started: {}", POSTGRES.getJdbcUrl());
            } else {
                String msg = "Docker environment not available. Ensure Docker Desktop/Engine is running or set DOCKER_HOST. " +
                        "Cucumber integration tests require Testcontainers (PostgreSQL).";
                LOG.error(msg);
                throw new IllegalStateException(msg);
            }
        } catch (Exception e) {
            // Log a clear error and re-throw to fail fast with helpful diagnostic information.
            LOG.error("Failed to initialize Testcontainers PostgreSQL for Cucumber tests: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Inject Testcontainer connection properties into the Spring context
     * before any bean is created.  Also forces every schema reference to
     * {@code public} so that Liquibase and Hibernate agree with PostgreSQL's
     * default schema inside the container.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Override all schema references to "public" (Testcontainers default)
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
        registry.add("spring.datasource.hikari.schema", () -> "public");
        registry.add("spring.liquibase.default-schema", () -> "public");
        registry.add("spring.liquibase.liquibase-schema", () -> "public");
    }
}

