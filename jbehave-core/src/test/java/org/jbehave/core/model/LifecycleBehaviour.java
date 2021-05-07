package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class LifecycleBehaviour {

    @Test
    void shouldCreateEmptyLifecycleWithDefaultConstructor() {
        Lifecycle lifecycle = new Lifecycle();
        assertThat(lifecycle.isEmpty(), is(true));
    }
}
