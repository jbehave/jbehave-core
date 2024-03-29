package org.jbehave.examples.core.stories.failing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.When;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.examples.core.stories.failing.EnsuringOrderUsingSteps.A;
import org.jbehave.examples.core.stories.failing.EnsuringOrderUsingSteps.B;
import org.jbehave.examples.core.stories.failing.EnsuringOrderUsingSteps.C;
import org.jbehave.examples.core.stories.failing.EnsuringOrderUsingSteps.MyStoryReporterBuilder;
import org.junit.runner.RunWith;

@RunWith(AnnotatedEmbedderRunner.class)
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = false,
        ignoreFailureInView = true)
@UsingSteps(instances = { A.class, B.class, C.class, EnsuringOrderUsingSteps.class })
@Configure(storyReporterBuilder = MyStoryReporterBuilder.class)
public class EnsuringOrderUsingSteps implements Embeddable {

    private Embedder embedder;

    @Override
    public void useEmbedder(Embedder embedder) {
        this.embedder = embedder;
    }

    @Override
    @org.junit.Test
    public void run() {
        embedder.runStoriesAsPaths(
                Arrays.asList("org/jbehave/examples/core/stories/failing/ensuring_order_using_steps.story"));
    }

    private static final List<String> ORDER = new LinkedList<>();

    @AfterScenario
    public void clearOrder() {
        ORDER.clear();
    }

    public static class MyStoryReporterBuilder extends StoryReporterBuilder {
        public MyStoryReporterBuilder() {
            withFormats(Format.CONSOLE);
        }

    }

    public static class A {
        @BeforeScenario
        public void add() {
            ORDER.add(getClass().getSimpleName());
        }
    }

    public static class B {
        @BeforeScenario
        public void add() {
            ORDER.add(getClass().getSimpleName());
        }
    }

    public static class C {
        @BeforeScenario
        public void add() {
            ORDER.add(getClass().getSimpleName());
        }
    }

    @When("running @BeforeScenario")
    public void noop() {
    }

    @Then("$left should be called before $right")
    public void assertOrder(String left, String right) {
        int leftIndex = ORDER.indexOf(left);
        int rightIndex = ORDER.indexOf(right);

        assertThat(leftIndex < rightIndex, is(true));
    }
}
