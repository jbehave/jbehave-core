package org.jbehave.core.junit.weld;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.configuration.weld.WeldAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.junit.weld.WeldAnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

public class WeldAnnotatedEmbedderRunnerBehaviour {

    @Test
    public void shouldCreateWithGuiceAnnotatedBuilder() throws InitializationError{
        AnnotatedEmbedderRunner runner = new WeldAnnotatedEmbedderRunner(RunningWithAnnotatedEmbedderRunner.class);
    
        assertThat(runner.annotationBuilder(), instanceOf(WeldAnnotationBuilder.class));
    }
    
    @RunWith(WeldAnnotatedEmbedderRunner.class)
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
