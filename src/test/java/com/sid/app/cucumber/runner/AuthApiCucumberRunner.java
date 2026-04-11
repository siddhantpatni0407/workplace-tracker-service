package com.sid.app.cucumber.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * JUnit 5 Platform Suite runner for the Auth API Cucumber scenarios.
 *
 * <p>Execute with:
 * <pre>
 *   ./gradlew cucumberTest
 * </pre>
 *
 * <p>Reports are written to:
 * <ul>
 *   <li>HTML  – {@code build/reports/cucumber/auth-api-report.html}</li>
 *   <li>JSON  – {@code build/reports/cucumber/auth-api-report.json}</li>
 *   <li>JUnit XML – {@code build/reports/cucumber/auth-api-report.xml}</li>
 * </ul>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/auth")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.sid.app.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, "
                + "html:build/reports/cucumber/auth-api-report.html, "
                + "json:build/reports/cucumber/auth-api-report.json, "
                + "junit:build/reports/cucumber/auth-api-report.xml")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,
        value = "not @Ignored")
public class AuthApiCucumberRunner {
    // This class is intentionally empty – the @Suite annotations drive execution.
}

