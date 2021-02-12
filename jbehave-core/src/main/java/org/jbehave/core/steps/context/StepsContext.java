package org.jbehave.core.steps.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.ToContext;

/**
 * Holds runtime context-related objects.
 */
public class StepsContext {

    private static final String OBJECT_ALREADY_STORED_MESSAGE = "Object key '%s' has been already stored before.";
    private static final String OBJECT_NOT_STORED_MESSAGE = "Object key '%s' has not been stored";

    private static final ThreadLocal<Map<String, Object>> exampleObjects = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> scenarioObjects = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> storyObjects = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> keysStored = new ThreadLocal<>();

    public void put(String key, Object object, ToContext.RetentionLevel retentionLevel) {
        checkForDuplicate(key);
        Map<String, Object> objects;
        if (ToContext.RetentionLevel.EXAMPLE.equals(retentionLevel)) {
            objects = getExampleObjects();
        } else if (ToContext.RetentionLevel.SCENARIO.equals(retentionLevel)) {
            objects = getScenarioObjects();
        } else {
            objects = getStoryObjects();
        }
        objects.put(key, object);
    }

    private void checkForDuplicate(String key) {
        Set<String> keys = keysStored.get();
        if (keys.contains(key)) {
            throw new ObjectAlreadyStoredException(String.format(OBJECT_ALREADY_STORED_MESSAGE, key));
        } else {
            keys.add(key);
        }
    }

    public Object get(String key) {
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

    private Map<String, Object> getExampleObjects() {
        Map<String, Object> objects = exampleObjects.get();
        if (objects == null) {
            objects = new HashMap<>();
            exampleObjects.set(objects);
        }
        return objects;
    }

    private Map<String, Object> getScenarioObjects() {
        Map<String, Object> objects = scenarioObjects.get();
        if (objects == null) {
            objects = new HashMap<>();
            scenarioObjects.set(objects);
        }
        return objects;
    }

    private Map<String, Object> getStoryObjects() {
        Map<String, Object> objects = storyObjects.get();
        if (objects == null) {
            objects = new HashMap<>();
            storyObjects.set(objects);
        }
        return objects;
    }

    private Set<String> getKeys() {
        Set<String> keys = keysStored.get();
        if (keys == null) {
            keys = new HashSet<>();
            keysStored.set(keys);
        }
        return keys;
    }

    public void resetExample() {
        Set<String> keys = getKeys();
        keys.removeAll(getExampleObjects().keySet());
        exampleObjects.set(new HashMap<String, Object>());
    }

    public void resetScenario() {
        Set<String> keys = getKeys();
        keys.removeAll(getScenarioObjects().keySet());
        scenarioObjects.set(new HashMap<String, Object>());
    }

    public void resetStory() {
        storyObjects.set(new HashMap<String, Object>());
        keysStored.set(new HashSet<String>());
    }

    @SuppressWarnings("serial")
    public static class ObjectNotStoredException extends RuntimeException {

        public ObjectNotStoredException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("serial")
    public static class ObjectAlreadyStoredException extends RuntimeException {

        public ObjectAlreadyStoredException(String message) {
            super(message);
        }
    }

}
