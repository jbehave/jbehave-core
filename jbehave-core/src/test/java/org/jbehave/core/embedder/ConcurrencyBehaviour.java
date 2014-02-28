package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.XML;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder.RunningEmbeddablesFailed;
import org.jbehave.core.embedder.StoryManager.StoryExecutionFailed;
import org.jbehave.core.embedder.StoryManager.StoryTimeout;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.TxtOutput;
import org.jbehave.core.reporters.XmlOutput;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.Test;

public class ConcurrencyBehaviour {

    @Test
    public void shouldCompleteXmlReportWhenStoryIsCancelled() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeoutInSecs(1);
        try {
            embedder.runAsEmbeddables(asList(XmlFormat.class.getName()));
        } catch (RunningEmbeddablesFailed e) {
            String xmlOutput = XmlFormat.out.toString();
            assertThat(xmlOutput, containsString("</scenario>"));
            assertThat(xmlOutput, containsString("</story>"));
        }
    }

    @Test(expected = RunningEmbeddablesFailed.class)
    public void shouldAllowStoriesToBeCancelled() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeoutInSecs(1);
        embedder.runAsEmbeddables(asList(MississipiCancelled.class.getName()));
    }

    @Test
    public void shouldFailOnTimeout() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeoutInSecs(1).doFailOnStoryTimeout(true);
        try {
            embedder.runAsEmbeddables(asList(XmlFormat.class.getName()));
        } catch (RunningEmbeddablesFailed e) {
            assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
            assertThat(e.getCause().getCause(), instanceOf(StoryTimeout.class));
        }
    }

    
    public static class MississipiCancelled extends JUnitStories {

        @Override
        public Configuration configuration() {
            return new MostUsefulConfiguration().useStoryLoader(new LoadFromClasspath(this.getClass()))
                    .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(CONSOLE, HTML, XML));
        }

        @Override
        public InjectableStepsFactory stepsFactory() {
            return new InstanceStepsFactory(configuration(), this);
        }

        @Override
        protected List<String> storyPaths() {
            return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        }

        @When("$name counts to $n Mississippi")
        public void whenSomeoneCountsMississippis(String name, AtomicInteger n) throws InterruptedException {
            long start = System.currentTimeMillis();
            System.out.println(name + " starts counting to " + n);
            for (int i = 0; i < n.intValue(); i++) {
                System.out.println(name + " says " + i + " Mississippi (" + (System.currentTimeMillis() - start)
                        + " millis)");
                TimeUnit.SECONDS.sleep(1);
            }
        }

    }

    public static class XmlFormat extends JUnitStory {
        static ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);

        public XmlFormat() {
            Embedder embeeder = configuredEmbedder();
            embeeder.embedderControls().useStoryTimeoutInSecs(1L);
        }

        @Override
        public Configuration configuration() {
            final XmlOutput xmlOutput = new XmlOutput(printStream);
            
            return new MostUsefulConfiguration().useStoryLoader(new MyStoryLoader()).useStoryReporterBuilder(
                    new StoryReporterBuilder() {
                        @Override
                        public StoryReporter build(String storyPath) {
                            if (storyPath.contains("format")) {
                                return xmlOutput;
                            } else {
                                return new TxtOutput(new PrintStream(new ByteArrayOutputStream()));
                            }

                        }
                    }.withFormats(Format.XML));
        }

        @Override
        public InjectableStepsFactory stepsFactory() {
            return new InstanceStepsFactory(this.configuration(), this);
        }

        @Given("something too long")
        public void somethingLong() throws Exception {
            Thread.sleep(5000L);
        }


        public static class MyStoryLoader implements StoryLoader {

            public String loadStoryAsText(String storyPath) {
                return "Scenario: \nGiven something too long";
            }

        }
    }
}
