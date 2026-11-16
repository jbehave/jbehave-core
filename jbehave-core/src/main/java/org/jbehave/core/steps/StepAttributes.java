package org.jbehave.core.steps;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Named string values attached to the step that is being executed. They are reported through
 * {@link org.jbehave.core.reporters.StoryReporter#stepAttributes(Map)} right before the step's outcome and rendered
 * by the built-in outputs inside that step. All instances share the same per-thread state.
 */
public class StepAttributes {

    private static final ThreadLocal<Deque<Map<String, String>>> SCOPES = new ThreadLocal<>();

    /**
     * Sets an attribute of the current step, replacing the value of an attribute with the same name. Does nothing if
     * no step is in progress on the current thread.
     *
     * @param name the attribute name
     * @param value the attribute value
     */
    public void put(String name, String value) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");
        Map<String, String> scope = currentScope();
        if (scope != null) {
            scope.put(name, value);
        }
    }

    /**
     * Returns the value of an attribute of the current step.
     *
     * @param name the attribute name
     * @return the value, or {@code null} when the attribute is absent or no step is in progress
     */
    public String get(String name) {
        Map<String, String> scope = currentScope();
        return scope != null ? scope.get(name) : null;
    }

    /**
     * Reports whether a step is collecting attributes on the current thread.
     *
     * @return {@code true} when a step scope is open
     */
    public boolean isStepInProgress() {
        return currentScope() != null;
    }

    /**
     * Opens the scope of a step. Called by JBehave.
     */
    public void startStep() {
        Deque<Map<String, String>> scopes = SCOPES.get();
        if (scopes == null) {
            scopes = new ArrayDeque<>();
            SCOPES.set(scopes);
        }
        scopes.push(new LinkedHashMap<String, String>());
    }

    /**
     * Closes the scope of the current step. Called by JBehave.
     *
     * @return the attributes set in the closed scope, in insertion order
     */
    public Map<String, String> finishStep() {
        Deque<Map<String, String>> scopes = SCOPES.get();
        Map<String, String> attributes = scopes.pop();
        if (scopes.isEmpty()) {
            SCOPES.remove();
        }
        return Collections.unmodifiableMap(attributes);
    }

    private static Map<String, String> currentScope() {
        Deque<Map<String, String>> scopes = SCOPES.get();
        return scopes != null ? scopes.peek() : null;
    }
}
