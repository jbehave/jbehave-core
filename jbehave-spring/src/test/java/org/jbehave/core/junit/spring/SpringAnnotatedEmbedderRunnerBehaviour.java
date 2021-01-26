package org.jbehave.core.junit.spring;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.configuration.spring.SpringAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class SpringAnnotatedEmbedderRunnerBehaviour {

    @Test
    void shouldCreateWithSpringAnnotatedBuilder() throws InitializationError{
        AnnotatedEmbedderRunner runner = new SpringAnnotatedEmbedderRunner(RunningWithAnnotatedEmbedderRunner.class);
        assertThat(runner.annotationBuilder(), instanceOf(SpringAnnotationBuilder.class));
    }

    @RunWith(SpringAnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class RunningWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        static boolean hasRun;

        @Override
        @org.junit.Test
        public void run() {
            hasRun = true;
        }
    }
}
