package org.jbehave.examples.trader.needle;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.needle.UsingNeedle;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.needle.NeedleAnnotatedEmbedderRunner;
import org.jbehave.examples.trader.needle.provider.TraderServiceInjectionProvider;
import org.junit.runner.RunWith;

@RunWith(NeedleAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingNeedle(provider = TraderServiceInjectionProvider.class)
public abstract class ParentAnnotatedEmbedderUsingNeedle extends InjectableEmbedder {

}
