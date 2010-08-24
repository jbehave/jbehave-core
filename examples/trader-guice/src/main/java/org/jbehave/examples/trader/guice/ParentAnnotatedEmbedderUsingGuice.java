package org.jbehave.examples.trader.guice;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.guice.GuiceAnnotatedEmbedderRunner;
import org.jbehave.examples.trader.guice.AnnotatedEmbedderUsingGuice.ConfigurationModule;
import org.junit.runner.RunWith;

@RunWith(GuiceAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingGuice(modules = { ConfigurationModule.class })
public abstract class ParentAnnotatedEmbedderUsingGuice extends InjectableEmbedder {

}
