package org.jbehave.core.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LifecycleBehaviour {

    @Test
    public void shouldCreateEmptyLifecycleWithDefaultConstructor() {
        Lifecycle lifecycle = new Lifecycle();
        assertThat(lifecycle.isEmpty(), is(true));
    }
}
