package org.jbehave.examples.core.spring;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.spring.SpringAnnotatedPathRunner;
import org.junit.runner.RunWith;

/**
 * Run stories via annotated embedder configuration and steps using Spring. The
 * textual trader stories are exactly the same ones found in the
 * jbehave-trader-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(SpringAnnotatedPathRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingSpring(resources = { "org/jbehave/examples/trader/spring/configuration.xml",
        "org/jbehave/examples/trader/spring/steps.xml" })
@UsingPaths(searchIn = "../core/src/main/java", includes = { "**/*.story" }, excludes = { "**/examples_table*.story" })
public class AnnotatedPathRunnerUsingSpring {

}
