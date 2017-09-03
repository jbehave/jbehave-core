package org.jbehave.core.embedder.executors;

import java.util.concurrent.ExecutorService;

import org.jbehave.core.embedder.EmbedderControls;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.instanceOf;

public class ExecutorsBehaviour {

    @Test
    public void shouldCreateExecutors() {
        assertThat(new FixedThreadExecutors().create(new EmbedderControls()), instanceOf(ExecutorService.class));
        assertThat(new DirectExecutorService().create(new EmbedderControls()), instanceOf(ExecutorService.class));
        assertThat(new SameThreadExecutors().create(new EmbedderControls()), instanceOf(ExecutorService.class));
    }
  
}
