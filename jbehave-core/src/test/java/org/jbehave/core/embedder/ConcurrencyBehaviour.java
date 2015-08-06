package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.XML;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
import org.jbehave.core.embedder.StoryManager.StoryTimedOut;
import org.jbehave.core.embedder.StoryTimeouts.TimeoutFormatException;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.CrossReference;
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
        embedder.embedderControls().useStoryTimeouts("1");
        try {
            embedder.runAsEmbeddables(asList(XmlFormat.class.getName()));
            fail("Exception was not thrown");
        } catch (RunningEmbeddablesFailed e) {
            String xmlOutput = XmlFormat.out.toString();
            assertThat(xmlOutput, containsString("</scenario>"));
            assertThat(xmlOutput, containsString("</story>"));
        }
    }

	@Test(expected = RunningEmbeddablesFailed.class)
	public void shouldAllowStoriesToBeCancelled() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeouts("1");
		embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	}
	
	@Test(expected = RunningEmbeddablesFailed.class)
	public void shouldAllowStoriesToBeCancelledByPaths() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeouts("**/*.story:1");
		embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	}

	@Test
	public void shouldAllowStoriesToBeTimed() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeouts("10").useThreads(2).doVerboseFailures(true);
		try {
			embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		} finally {
			embedder.generateCrossReference();
		}
	}

	@Test
	public void shouldFailOnTimeout() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeouts("1")
				.doFailOnStoryTimeout(true);
		try {
			embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
			fail("Exception was not thrown");
		} catch (RunningEmbeddablesFailed e) {
			assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
			assertThat(e.getCause().getCause(), instanceOf(StoryTimedOut.class));
		}
	}
	 
	 @Test
	 public void shouldFailOnTimeoutWhenSpecifiedByPath() {
		 Embedder embedder = new Embedder();
         embedder.embedderControls().useStoryTimeouts("**/*.story:1").doFailOnStoryTimeout(true);
         try {
             embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
             fail("Exception was not thrown");
         } catch (RunningEmbeddablesFailed e) {
            assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
            assertThat(e.getCause().getCause(), instanceOf(StoryTimedOut.class));
         }
	 }
	 
	 @Test
	 public void shouldUseDefaultTimeoutWhenNoTimeoutsAreSpecified() {
	     OutputStream out = new ByteArrayOutputStream();
	     Embedder embedder = new Embedder(embedderMonitor(out));
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
   	     embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	     assertThat(out.toString(), containsString("Using timeout for story a_short.story of 300"));
   	     ThreadsStories.setCustomStoryPath(null);
	 }
	 
	 @Test
	 public void shouldUseTimeoutByPathIfSpecifiedOrDefaultOtherwise() {
	     OutputStream out = new ByteArrayOutputStream();
	     Embedder embedder = new Embedder(embedderMonitor(out));
		 try {
			 embedder.embedderControls().useStoryTimeouts("**/another_long.story:1").doFailOnStoryTimeout(true);
			 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
			 fail("Exception was not thrown");
		 } catch (RunningEmbeddablesFailed e) {
			 assertThat(out.toString(), containsString("Using timeout for story a_short.story of 300"));
			 assertThat(out.toString(), containsString("Using timeout for story a_long.story of 300"));
			 assertThat(out.toString(), containsString("Using timeout for story another_long.story of 1"));
		 }
	 }
	 
	 @Test
	 public void shouldUseTheTimeoutTypeSpecified() {
	     OutputStream out = new ByteArrayOutputStream();
	     Embedder embedder = new Embedder(embedderMonitor(out));
		 try {
			 embedder.embedderControls().useStoryTimeouts("10,**/a_short.story:1,**/another_long.story:2").doFailOnStoryTimeout(true);
		   	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
			 fail("Exception was not thrown");
		 } catch (RunningEmbeddablesFailed e) {
			 assertThat(out.toString(), containsString("Using timeout for story a_short.story of 1"));
			 assertThat(out.toString(), containsString("Using timeout for story a_long.story of 10"));
			 assertThat(out.toString(), containsString("Using timeout for story another_long.story of 2"));
		}
	 }
	 
	 @Test
	 public void shouldHandleBothTimeoutTypesSpecifiedAsTheSame() {
	     OutputStream out = new ByteArrayOutputStream();
	     Embedder embedder = new Embedder(embedderMonitor(out));
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
		 embedder.embedderControls().useStoryTimeouts("7,**/a_short.story:7").doFailOnStoryTimeout(true);
		 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName())); 
      	 assertThat(out.toString(), containsString("Using timeout for story a_short.story of 7"));
         ThreadsStories.setCustomStoryPath(null);
	 }
	 
	 @Test
	 public void shouldAllowTimeoutByPathToBeZeroMeaningNoTimeLimit() {
	     OutputStream out = new ByteArrayOutputStream();
	     Embedder embedder = new Embedder(embedderMonitor(out));
		 ThreadsStories.setCustomStoryPath("**/another_long.story");
		 embedder.embedderControls().useStoryTimeouts("**/another_long.story:0").doFailOnStoryTimeout(true);
		 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		 assertThat(out.toString(), containsString("Using timeout for story another_long.story of 0"));
		 ThreadsStories.setCustomStoryPath(null);
	 }
	 
	 @Test
	 public void shouldUseDefaultTimeoutWhenTimeoutByPathIsEmpty() {
	     OutputStream out = new ByteArrayOutputStream();
	     Embedder embedder = new Embedder(embedderMonitor(out));
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
		 embedder.embedderControls().useStoryTimeouts("").doFailOnStoryTimeout(true);
		 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));  
      	 assertThat(out.toString(), containsString("Using timeout for story a_short.story of 300"));
	     ThreadsStories.setCustomStoryPath(null);
	 }

	 @Test
	 public void shouldAllowMultipleTimeoutsByPathToBeSpecified() {
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
	 public void shouldNotAllowTimeoutByPathToBeInvalidFormats() {
		 assertThatStoryTimeoutIsInvalid("**/a_long.story:adhgaldsh");
		 assertThatStoryTimeoutIsInvalid("**/a_long.story:       ");
	 }

	private void assertThatStoryTimeoutIsInvalid(String storyTimeouts) {
		try {
			Embedder embedder = new Embedder();
			embedder.embedderControls().useStoryTimeouts(storyTimeouts)
					.doFailOnStoryTimeout(true);
			embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
			fail("Exception was not thrown");
		} catch (RunningEmbeddablesFailed e) {
			assertThat(e.getCause(), instanceOf(TimeoutFormatException.class));
		}
	}
	 
	 @Test
	 public void shouldHandleRepeatedTimeoutsByPath() {
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
			return new MostUsefulConfiguration().useStoryLoader(
					new LoadFromClasspath(this.getClass()))
					.useStoryReporterBuilder(
							new StoryReporterBuilder().withFormats(CONSOLE,
									HTML, XML).withCrossReference(new CrossReference()));
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
        static ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);

        public XmlFormat() {
            Embedder embeeder = configuredEmbedder();
            embeeder.embedderControls().useStoryTimeouts("1");
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

			public String loadResourceAsText(String resourcePath) {
				return null;
			}

        }
    }

}
