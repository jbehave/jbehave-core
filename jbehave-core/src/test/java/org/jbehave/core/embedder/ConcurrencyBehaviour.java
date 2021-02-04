package org.jbehave.core.embedder;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder.RunningEmbeddablesFailed;
import org.jbehave.core.embedder.StoryManager.StoryExecutionFailed;
import org.jbehave.core.embedder.StoryManager.StoryTimedOut;
import org.jbehave.core.embedder.StoryTimeouts.TimeoutFormatException;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.*;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConcurrencyBehaviour {

    @Test
    void shouldCompleteXmlReportWhenStoryIsCancelled() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeouts("1");
        try {
            embedder.runAsEmbeddables(asList(XmlFormat.class.getName()));
            throw new AssertionError("Exception was not thrown");
        } catch (RunningEmbeddablesFailed e) {
            String xmlOutput = XmlFormat.xmlOut.toString();
            assertThat(xmlOutput, containsString("</scenario>"));
            assertThat(xmlOutput, containsString("</story>"));
        }
    }

    @Test
    void shouldCompleteTextReportWhenStoryIsFinishedSuccessfully() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeouts("4");
        embedder.embedderControls().useThreads(2);
        embedder.runAsEmbeddables(asList(XmlFormat.class.getName()));
        String textOutput = XmlFormat.textOut.toString();
        assertThat(textOutput,
                equalTo("beforeStory: BeforeStories\nafterStory\nbeforeStory: AfterStories\nafterStory\n"));
    }

    @Test
    void shouldAllowStoriesToBeCancelled() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeouts("1");
        List<String> classNames = singletonList(ThreadsStories.class.getName());
        assertThrows(RunningEmbeddablesFailed.class, () -> embedder.runAsEmbeddables(classNames));
    }

    @Test
    void shouldAllowStoriesToBeCancelledByPaths() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeouts("**/*.story:1");
        List<String> classNames = singletonList(ThreadsStories.class.getName());
        assertThrows(RunningEmbeddablesFailed.class, () -> embedder.runAsEmbeddables(classNames));
    }

    @Test
    void shouldAllowStoriesToBeTimed() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeouts("10").useThreads(2).doVerboseFailures(true);
        embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
    }

    @Test
    void shouldFailOnTimeout() {
        Embedder embedder = new Embedder();
        embedder.embedderControls().useStoryTimeouts("1")
                .doFailOnStoryTimeout(true);
        try {
            embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
            throw new AssertionError("Exception was not thrown");
        } catch (RunningEmbeddablesFailed e) {
            assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
            assertThat(e.getCause().getCause(), instanceOf(StoryTimedOut.class));
        }
    }

     @Test
     void shouldFailOnTimeoutWhenSpecifiedByPath() {
         Embedder embedder = new Embedder();
         embedder.embedderControls().useStoryTimeouts("**/*.story:1").doFailOnStoryTimeout(true);
         try {
             embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
             throw new AssertionError("Exception was not thrown");
         } catch (RunningEmbeddablesFailed e) {
            assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
            assertThat(e.getCause().getCause(), instanceOf(StoryTimedOut.class));
         }
     }

     @Test
     void shouldUseDefaultTimeoutWhenNoTimeoutsAreSpecified() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         ThreadsStories.setCustomStoryPath("**/a_short.story");
         embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
         assertThat(out.toString(), containsString("Using timeout for story a_short.story of 300"));
         ThreadsStories.setCustomStoryPath(null);
     }

     @Test
     void shouldUseTimeoutByPathIfSpecifiedOrDefaultOtherwise() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         try {
             embedder.embedderControls().useStoryTimeouts("**/another_long.story:1").doFailOnStoryTimeout(true);
             embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
             throw new AssertionError("Exception was not thrown");
         } catch (RunningEmbeddablesFailed e) {
             assertThat(out.toString(), containsString("Using timeout for story a_short.story of 300"));
             assertThat(out.toString(), containsString("Using timeout for story a_long.story of 300"));
             assertThat(out.toString(), containsString("Using timeout for story another_long.story of 1"));
         }
     }

     @Test
     void shouldUseTheTimeoutTypeSpecified() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         try {
             embedder.embedderControls().useStoryTimeouts("10,**/a_short.story:1,**/another_long.story:2").doFailOnStoryTimeout(true);
             embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
             throw new AssertionError("Exception was not thrown");
         } catch (RunningEmbeddablesFailed e) {
             assertThat(out.toString(), containsString("Using timeout for story a_short.story of 1"));
             assertThat(out.toString(), containsString("Using timeout for story a_long.story of 10"));
             assertThat(out.toString(), containsString("Using timeout for story another_long.story of 2"));
        }
     }

     @Test
     void shouldHandleBothTimeoutTypesSpecifiedAsTheSame() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         ThreadsStories.setCustomStoryPath("**/a_short.story");
         embedder.embedderControls().useStoryTimeouts("7,**/a_short.story:7").doFailOnStoryTimeout(true);
         embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
         assertThat(out.toString(), containsString("Using timeout for story a_short.story of 7"));
         ThreadsStories.setCustomStoryPath(null);
     }

     @Test
     void shouldAllowTimeoutByPathToBeZeroMeaningNoTimeLimit() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         ThreadsStories.setCustomStoryPath("**/another_long.story");
         embedder.embedderControls().useStoryTimeouts("**/another_long.story:0").doFailOnStoryTimeout(true);
         embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
         assertThat(out.toString(), containsString("Using timeout for story another_long.story of 0"));
         ThreadsStories.setCustomStoryPath(null);
     }

     @Test
     void shouldUseDefaultTimeoutWhenTimeoutByPathIsEmpty() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         ThreadsStories.setCustomStoryPath("**/a_short.story");
         embedder.embedderControls().useStoryTimeouts("").doFailOnStoryTimeout(true);
         embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
         assertThat(out.toString(), containsString("Using timeout for story a_short.story of 300"));
         ThreadsStories.setCustomStoryPath(null);
     }

     @Test
     void shouldAllowMultipleTimeoutsByPathToBeSpecified() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));

         // Note: **/another_long_TEST.story is an invalid path and therefore won't be considered
         String timeoutsByPath = "**/a_short.story:10,**/a_long.story:15,**/another_long_TEST.story:77";
         embedder.embedderControls().useStoryTimeouts(timeoutsByPath).doFailOnStoryTimeout(true);
         embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));

         assertThat(out.toString(), containsString("Using timeout for story a_short.story of 10"));
         assertThat(out.toString(), containsString("Using timeout for story a_long.story of 15"));
         assertThat(out.toString(), containsString("Using timeout for story another_long.story of 300"));
     }

     @Test
     void shouldNotAllowTimeoutByPathToBeInvalidFormats() {
         assertThatStoryTimeoutIsInvalid("**/a_long.story:adhgaldsh");
         assertThatStoryTimeoutIsInvalid("**/a_long.story:       ");
     }

    private void assertThatStoryTimeoutIsInvalid(String storyTimeouts) {
        try {
            Embedder embedder = new Embedder();
            embedder.embedderControls().useStoryTimeouts(storyTimeouts)
                    .doFailOnStoryTimeout(true);
            embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
            throw new AssertionError("Exception was not thrown");
        } catch (RunningEmbeddablesFailed e) {
            assertThat(e.getCause(), instanceOf(TimeoutFormatException.class));
        }
    }

     @Test
     void shouldHandleRepeatedTimeoutsByPath() {
         OutputStream out = new ByteArrayOutputStream();
         Embedder embedder = new Embedder(embedderMonitor(out));
         ThreadsStories.setCustomStoryPath("**/a_short.story");
         embedder.embedderControls().useStoryTimeouts("**/a_short.story:25,**/a_short.story:25").doFailOnStoryTimeout(true);
         embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
         assertThat(out.toString(), containsString("Using timeout for story a_short.story of 25"));
         ThreadsStories.setCustomStoryPath(null);
     }

     private EmbedderMonitor embedderMonitor(OutputStream out) {
         return new PrintStreamEmbedderMonitor(new PrintStream(out));
     }


     public static class ThreadsStories extends JUnitStories {

        /* Since some Unit Tests require stories to run to completion, 'customStoryPath' can be used
        to run specific tests and limit test running time */
        private static String customStoryPath = null;

        @Override
        public Configuration configuration() {
            return new MostUsefulConfiguration()
                    .useStoryLoader(new LoadFromClasspath(this.getClass()))
                    .useStoryReporterBuilder(new StoryReporterBuilder()
                            .withFormats(CONSOLE, HTML, XML, JSON));
        }

        @Override
        public InjectableStepsFactory stepsFactory() {
            return new InstanceStepsFactory(configuration(), new ThreadsSteps());
        }

        public static void setCustomStoryPath(String customStoryPath) {
            ThreadsStories.customStoryPath = customStoryPath;
        }

        @Override
        protected List<String> storyPaths() {
            if(customStoryPath != null) {
                return new StoryFinder().findPaths(
                        codeLocationFromClass(this.getClass()), customStoryPath, "");
            }
            else {
                return new StoryFinder().findPaths(
                        codeLocationFromClass(this.getClass()), "**/*.story", "");
            }

        }

    }

    public static class ThreadsSteps {

        @When("$name counts to $n Mississippi")
        public void whenSomeoneCountsMississippis(String name, AtomicInteger n)
                throws InterruptedException {
            long start = System.currentTimeMillis();
            System.out.println(name + " starts counting to " + n);
            for (int i = 0; i < n.intValue(); i++) {
                System.out.println(name + " says " + i + " Mississippi ("
                        + (System.currentTimeMillis() - start) + " millis)");
                TimeUnit.SECONDS.sleep(1);
            }
        }

    }
    
    public static class XmlFormat extends JUnitStory {
        static ByteArrayOutputStream xmlOut;
        static ByteArrayOutputStream textOut;

        XmlFormat() {
            xmlOut = new ByteArrayOutputStream();
            textOut = new ByteArrayOutputStream();
        }

        @Override
        public Configuration configuration() {
            return new MostUsefulConfiguration().useStoryLoader(new MyStoryLoader()).useStoryReporterBuilder(
                    new StoryReporterBuilder() {
                        @Override
                        public StoryReporter build(String storyPath) {
                            return new DelegatingStoryReporter(super.build(storyPath));
                        }

                        @Override
                        public StoryReporter reporterFor(String storyPath, org.jbehave.core.reporters.Format format) {
                            if (storyPath.contains("format")) {
                                return new XmlOutput(new PrintStream(xmlOut));
                            } else {
                                Properties properties = new Properties();
                                properties.put("beforeStory", "beforeStory: {2}\n");
                                properties.put("afterStory", "afterStory\n");
                                return new TxtOutput(new PrintStream(textOut), properties);
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
            Thread.sleep(2500L);
        }

        public static class MyStoryLoader implements StoryLoader {

            @Override
            public String loadStoryAsText(String storyPath) {
                return "Scenario: \nGiven something too long";
            }

            @Override
            public String loadResourceAsText(String resourcePath) {
                return null;
            }
        }
    }
}
