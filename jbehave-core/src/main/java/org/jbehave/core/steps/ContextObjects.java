package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.annotations.ContextOutcome;

/**
 * Holds runtime context-related objects.
 */
public class ContextObjects {
    private static final ThreadLocal<Map<String, Object>> exampleObjects = new ThreadLocal<Map<String, Object>>();
    private static final ThreadLocal<Map<String, Object>> scenarioObjects = new ThreadLocal<Map<String, Object>>();
    private static final ThreadLocal<Map<String, Object>> storyObjects = new ThreadLocal<Map<String, Object>>();
    private static final String OBJECT_ALREADY_STORED_MESSAGE = "Object key '%s' has been already stored before.";
    private static final String OBJECT_NOT_STORED_MESSAGE = "Object key '%s' has not been stored";

    private ContextObjects() {
    }

    static void setObject(String key, Object object, ContextOutcome.RetentionLevel retentionLevel) {
        Map<String, Object> objects;
        if (ContextOutcome.RetentionLevel.EXAMPLE.equals(retentionLevel)) {
            objects = getExampleObjects();
        } else if (ContextOutcome.RetentionLevel.SCENARIO.equals(retentionLevel)) {
            objects = getScenarioObjects();
        } else {
            objects = getStoryObjects();
        }

        if (objects.containsKey(key)) {
            throw new ObjectAlreadyStoredException(String.format(OBJECT_ALREADY_STORED_MESSAGE, key));
        }
        objects.put(key, object);
    }

    static Object getObject(String key) {
        Object object = getExampleObjects().get(key);
        if (object == null) {
            object = getScenarioObjects().get(key);
            if (object == null) {
                object = getStoryObjects().get(key);
            }
        }

        if (object == null) {
            throw new ObjectNotStoredException(String.format(OBJECT_NOT_STORED_MESSAGE, key));
        }

        return object;
    }

    private static Map<String, Object> getExampleObjects() {
        Map<String, Object> objects = exampleObjects.get();
        return objects;
    }

    private static Map<String, Object> getScenarioObjects() {
        return scenarioObjects.get();
    }

    private static Map<String, Object> getStoryObjects() {
        return storyObjects.get();
    }

    public static void resetExampleObjects() {
        Map<String, Object> objects = new HashMap<String, Object>();
        exampleObjects.set(objects);
    }

    public static void resetScenarioObjects() {
        Map<String, Object> objects = new HashMap<String, Object>();
        scenarioObjects.set(objects);
    }

    public static void resetStoryObjects() {
        Map<String, Object> objects = new HashMap<String, Object>();
        storyObjects.set(objects);
    }

    public static class ObjectNotStoredException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ObjectNotStoredException(String message) {
            super(message);
        }
    }

    public static class ObjectAlreadyStoredException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ObjectAlreadyStoredException(String message) {
            super(message);
        }
    }

}
