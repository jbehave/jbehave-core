package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class StepAttributesBehaviour {

    private final StepAttributes attributes = new StepAttributes();

    @AfterEach
    void closeOpenScopes() {
        while (attributes.isStepInProgress()) {
            attributes.finishStep();
        }
    }

    @Test
    void shouldIgnoreAttributesSetOutsideAStep() {
        attributes.put("name", "value");

        assertThat(attributes.get("name"), nullValue());
        assertThat(attributes.isStepInProgress(), is(false));
    }

    @Test
    void shouldKeepAttributesInInsertionOrderAndReplaceValues() {
        attributes.startStep();
        attributes.put("colour", "blue");
        attributes.put("size", "large");
        attributes.put("colour", "red");

        assertThat(attributes.get("colour"), equalTo("red"));
        assertThat(attributes.get("size"), equalTo("large"));
        assertThat(attributes.isStepInProgress(), is(true));

        Map<String, String> finished = attributes.finishStep();
        assertThat(new ArrayList<>(finished.keySet()), equalTo(asList("colour", "size")));
        assertThat(finished.get("colour"), equalTo("red"));
        assertThat(finished.get("size"), equalTo("large"));
    }

    @Test
    void shouldIsolateNestedScopes() {
        attributes.startStep();
        attributes.put("outer", "1");
        attributes.startStep();
        attributes.put("inner", "2");

        assertThat(attributes.get("outer"), nullValue());
        assertThat(attributes.get("inner"), equalTo("2"));

        Map<String, String> inner = attributes.finishStep();
        assertThat(inner.get("inner"), equalTo("2"));
        assertThat(inner.containsKey("outer"), is(false));
        assertThat(attributes.get("outer"), equalTo("1"));
        assertThat(attributes.isStepInProgress(), is(true));

        attributes.finishStep();
        assertThat(attributes.isStepInProgress(), is(false));
    }

    @Test
    void shouldLeaveNoStepInProgressAfterTheLastScopeIsClosed() {
        attributes.startStep();
        attributes.finishStep();

        assertThat(attributes.isStepInProgress(), is(false));
    }

    @Test
    void shouldReturnAnUnmodifiableMap() {
        attributes.startStep();
        attributes.put("name", "value");

        Map<String, String> finished = attributes.finishStep();

        assertThrows(UnsupportedOperationException.class, () -> finished.put("other", "nope"));
    }

    @Test
    void shouldShareStateBetweenInstances() {
        StepAttributes other = new StepAttributes();
        attributes.startStep();
        attributes.put("name", "value");

        assertThat(other.get("name"), equalTo("value"));
        other.put("name", "replaced");
        assertThat(attributes.get("name"), equalTo("replaced"));
    }

    @Test
    void shouldNotSeeAttributesFromAnotherThread() throws Exception {
        attributes.startStep();
        attributes.put("name", "value");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> seen = executor.submit(() -> {
                StepAttributes other = new StepAttributes();
                other.put("other", "nope");
                return other.get("name");
            });
            assertThat(seen.get(), nullValue());
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception ? (Exception) e.getCause() : e;
        } finally {
            executor.shutdownNow();
        }
        assertThat(attributes.get("name"), equalTo("value"));
    }

    @Test
    void shouldRejectNullNameOrValue() {
        attributes.startStep();

        assertThrows(NullPointerException.class, () -> attributes.put(null, "value"));
        assertThrows(NullPointerException.class, () -> attributes.put("name", null));
        assertThat(attributes.get("name"), nullValue());
    }
}
