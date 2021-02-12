package org.jbehave.examples.groovy;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.groovy.UsingGroovy;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.groovy.GroovyAnnotatedEmbedderRunner;
import org.junit.runner.RunWith;

import java.util.List;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

/**
 * Run stories via annotated embedder configuration and steps using Groovy.
 */
@RunWith(GroovyAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingGroovy()
public class AnnotatedEmbedderUsingGroovy extends InjectableEmbedder {

    @Override
    @org.junit.Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    public List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/using_groovy.story"), null);
    }
    
}
