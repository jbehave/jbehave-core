package org.jbehave.core.junit.groovy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.configuration.groovy.GroovyAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

public class GroovyAnnotatedEmbedderRunnerBehaviour {

    @Test
    public void shouldCreateWithSpringAnnotatedBuilder() throws InitializationError{
        AnnotatedEmbedderRunner runner = new GroovyAnnotatedEmbedderRunner(RunningWithAnnotatedEmbedderRunner.class);
        assertThat(runner.annotationBuilder(), instanceOf(GroovyAnnotationBuilder.class));
    }

    @RunWith(GroovyAnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class RunningWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        static boolean hasRun;

        @Override
        @Test
        public void run() {
            hasRun = true;
        }
    }
}
