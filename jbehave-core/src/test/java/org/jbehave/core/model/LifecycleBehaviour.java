package org.jbehave.core.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LifecycleBehaviour {

    @Test
    void shouldCreateEmptyLifecycleWithDefaultConstructor() {
        Lifecycle lifecycle = new Lifecycle();
        assertThat(lifecycle.isEmpty(), is(true));
    }
}
