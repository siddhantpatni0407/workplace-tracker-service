package com.sid.app.cucumber.hooks;

import com.sid.app.cucumber.context.ScenarioContext;
import com.sid.app.cucumber.helper.TestDataHelper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cucumber lifecycle hooks for the Auth API test suite.
 *
 * <ul>
 *   <li>{@code @Before}      – logs scenario start</li>
 *   <li>{@code @Before("@RequiresSeedData")} – seeds prerequisite DB data for tagged scenarios</li>
 *   <li>{@code @After}       – logs result and clears the {@link ScenarioContext}</li>
 *   <li>{@code @After("@RequiresSeedData")} – removes seed data after each tagged scenario</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class CucumberHooks {

    private final ScenarioContext scenarioContext;
    private final TestDataHelper testDataHelper;

    // ---------------------------------------------------------------
    // Before hooks
    // ---------------------------------------------------------------

    @Before(order = 10)
    public void logScenarioStart(Scenario scenario) {
        log.info("▶ SCENARIO START  [{}]  {}", scenario.getId(), scenario.getName());
    }

    // ---------------------------------------------------------------
    // After hooks
    // ---------------------------------------------------------------

    /**
     * Cleans up DB seed data after every scenario tagged {@code @RequiresSeedData}.
     * Runs before the general after-hook (order 10) so the context is still populated.
     */
    @After(value = "@RequiresSeedData", order = 20)
    public void cleanupSeedData(Scenario scenario) {
        log.info("↩ Cleaning up seed data for scenario: {}", scenario.getName());
        testDataHelper.cleanupSeedData();
    }

    @After(order = 10)
    public void logScenarioEnd(Scenario scenario) {
        log.info("■ SCENARIO END    [{}]  {} – {}",
                scenario.getId(), scenario.getName(), scenario.getStatus());
        scenarioContext.clear();
    }
}


