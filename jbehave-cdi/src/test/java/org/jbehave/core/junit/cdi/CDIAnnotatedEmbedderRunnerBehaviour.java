package org.jbehave.core.junit.cdi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.configuration.cdi.CDIAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.junit.cdi.CDIAnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

public class CDIAnnotatedEmbedderRunnerBehaviour {

    @Test
    public void shouldCreateWithGuiceAnnotatedBuilder() throws InitializationError{
        AnnotatedEmbedderRunner runner = new CDIAnnotatedEmbedderRunner(RunningWithAnnotatedEmbedderRunner.class);
    
        assertThat(runner.annotationBuilder(), instanceOf(CDIAnnotationBuilder.class));
    }
    
    @RunWith(CDIAnnotatedEmbedderRunner.class)
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
