package org.jbehave.core.junit.pico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.configuration.pico.PicoAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

public class PicoAnnotatedEmbedderRunnerBehaviour {

    @Test
    public void shouldCreateWithPicoAnnotatedBuilder() throws InitializationError{
        AnnotatedEmbedderRunner runner = new PicoAnnotatedEmbedderRunner(RunningWithAnnotatedEmbedderRunner.class);
        assertThat(runner.annotationBuilder(), instanceOf(PicoAnnotationBuilder.class));
    }

    @RunWith(PicoAnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class RunningWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        static boolean hasRun;

        @Test
        public void run() {
            hasRun = true;
        }
    }
}
