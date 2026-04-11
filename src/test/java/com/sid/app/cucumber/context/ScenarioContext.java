package com.sid.app.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Scenario-scoped Spring bean that acts as a shared blackboard between
 * step-definition classes within the same Cucumber scenario.
 *
 * <p>Common keys used across Auth step definitions:
 * <ul>
 *   <li>{@code jwtToken}      – JWT Bearer token after a successful login</li>
 *   <li>{@code registeredEmail}  – email address used during the current scenario's registration</li>
 *   <li>{@code registeredPassword} – plain-text password used during registration</li>
 * </ul>
 *
 * <p>The context is automatically cleared after each scenario via
 * {@link com.sid.app.cucumber.hooks.CucumberHooks}.
 */
@Component
@ScenarioScope
public class ScenarioContext {

    private final Map<String, Object> store = new HashMap<>();

    // Predefined context keys – use constants to avoid typos across classes
    public static final String JWT_TOKEN           = "jwtToken";
    public static final String REGISTERED_EMAIL    = "registeredEmail";
    public static final String REGISTERED_PASSWORD = "registeredPassword";
    public static final String LAST_REGISTERED_EMAIL = "lastRegisteredEmail";

    public void set(String key, Object value) {
        store.put(key, value);
    }

    public Object get(String key) {
        return store.get(key);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(store.get(key));
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }

    public void clear() {
        store.clear();
    }
}

