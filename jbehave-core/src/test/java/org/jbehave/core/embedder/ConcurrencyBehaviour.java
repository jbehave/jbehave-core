package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.XML;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
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
		embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	}
	
	@Test(expected = RunningEmbeddablesFailed.class)
	public void shouldAllowStoriesToBeCancelledByPaths() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeoutInSecsByPath("**/*.story:1");
		embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	}

	@Test
	public void shouldAllowStoriesToBeTimed() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeoutInSecs(10).useThreads(2).doVerboseFailures(true);
		try {
			embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		} finally {
			embedder.generateCrossReference();
		}
	}

	 @Test
	 public void shouldFailOnTimeout() {
	        Embedder embedder = new Embedder();
	        embedder.embedderControls().useStoryTimeoutInSecs(1).doFailOnStoryTimeout(true);
	        try {
	            embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	        } catch (RunningEmbeddablesFailed e) {
	            assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
	            assertThat(e.getCause().getCause(), instanceOf(StoryTimeout.class));
	        }
	    }
	 
	 @Test
	 public void shouldFailOnTimeoutWhenSpecifiedByPath() {
		 Embedder embedder = new Embedder();
         embedder.embedderControls().useStoryTimeoutInSecsByPath("**/*.story:1").doFailOnStoryTimeout(true);
         try {
             embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
         } catch (RunningEmbeddablesFailed e) {
            assertThat(e.getCause(), instanceOf(StoryExecutionFailed.class));
            assertThat(e.getCause().getCause(), instanceOf(StoryTimeout.class));
         }
	 }
	 
	 @Test
	 public void shouldUseDefaultTimeoutWhenNoTimeoutsAreSpecified() {
		 // Only running the shortest story because it needs to complete before 'timeoutValuesUsed' can be retrieved
		 Embedder embedder = new Embedder();
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
   	     embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
   	     Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
   	     assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(300)));
   	     ThreadsStories.setCustomStoryPath(null);
	 }
	 
	 @Test
	 public void shouldUseTimeoutByPathIfSpecifiedOrDefaultOtherwise() {
		 Embedder embedder = null;
		 try {
			 embedder = new Embedder();
			 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/another_long.story:1").doFailOnStoryTimeout(true);
		   	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		   	    
	        } catch (RunningEmbeddablesFailed e) {
	        	Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
	            assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(300)));
	            assertThat(timeoutValuesUsed.get("a_long.story"), equalTo(new Long(300)));
	            assertThat(timeoutValuesUsed.get("another_long.story"), equalTo(new Long(1)));
	        }
	 }
	 
	 @Test
	 public void shouldUseTheTimeoutTypeSpecified() {
		 Embedder embedder = null;
		 try {
			 embedder = new Embedder();
			 embedder.embedderControls().useStoryTimeoutInSecs(10).doFailOnStoryTimeout(true);
			 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/a_short.story:1,**/another_long.story:2").doFailOnStoryTimeout(true);
		   	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	        } catch (RunningEmbeddablesFailed e) {
	        	Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
	            assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(1)));
	            assertThat(timeoutValuesUsed.get("a_long.story"), equalTo(new Long(10)));
	            assertThat(timeoutValuesUsed.get("another_long.story"), equalTo(new Long(2)));
	        }
	 }
	 
	 @Test
	 public void shouldHandleBothTimeoutTypesSpecifiedAsTheSame() {
		 Embedder embedder = new Embedder();
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
		 embedder.embedderControls().useStoryTimeoutInSecs(7).doFailOnStoryTimeout(true);
		 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/a_short.story:7").doFailOnStoryTimeout(true);
		 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName())); 
		 Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
         assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(7)));
         ThreadsStories.setCustomStoryPath(null);
	 }
	 
	 @Test
	 public void shouldAllowTimeoutByPathToBeZero() {
		 Embedder embedder = null;
		 try {
			 embedder = new Embedder();
			 ThreadsStories.setCustomStoryPath("**/another_long.story");
			 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/another_long.story:0").doFailOnStoryTimeout(true);
		   	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));  
	        } catch (RunningEmbeddablesFailed e) {
	        	Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
	            assertThat(timeoutValuesUsed.get("another_long.story"), equalTo(new Long(0)));
	        }
		 finally {
			 ThreadsStories.setCustomStoryPath(null);
		 }
	 }
	 
	 @Test
	 public void shouldUseDefaultTimeoutWhenTimeoutByPathIsEmpty() {
		 Embedder embedder = new Embedder();
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
		 embedder.embedderControls().useStoryTimeoutInSecsByPath("").doFailOnStoryTimeout(true);
		 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));  
		 Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
	     assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(300)));
	     ThreadsStories.setCustomStoryPath(null);
	 }
	 
	 @Test
	 public void shouldAllowMultipleTimeoutsByPathToBeSpecified() {
		 Embedder embedder = new Embedder();
		 
		 // Note: **/another_long_TEST.story is an invalid path and therefore won't be considered
		 String timeoutsByPath = "**/a_short.story:10,**/a_long.story:15,**/another_long_TEST.story:77";
		 embedder.embedderControls().useStoryTimeoutInSecsByPath(timeoutsByPath).doFailOnStoryTimeout(true);
	   	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		   	    
		 Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
	     assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(10)));
	     assertThat(timeoutValuesUsed.get("a_long.story"), equalTo(new Long(15)));
	     assertThat(timeoutValuesUsed.get("another_long.story"), equalTo(new Long(300)));
	 }
	 
	 @Test
	 public void shouldNotAllowTimeoutByPathToBeInvalidFormat1() {
		 try {
			 Embedder embedder = new Embedder();
			 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/a_long.story:adhgaldsh").doFailOnStoryTimeout(true);
		 	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	        } catch (RunningEmbeddablesFailed e) {
	            assertThat(e.getCause(), instanceOf(NumberFormatException.class));
	        }
	 }
	 
	 @Test
	 public void shouldNotAllowTimeoutByPathToBeInvalidFormat2() {
		 try {
			 Embedder embedder = new Embedder();
			 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/a_long.story:       ").doFailOnStoryTimeout(true);
		 	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	        } catch (RunningEmbeddablesFailed e) {
	            assertThat(e.getCause(), instanceOf(NumberFormatException.class));
	        }
	 }
	 
	 @Test
	 public void shouldHandleRepeatedTimeoutsByPath() {
		 Embedder embedder = new Embedder();
		 ThreadsStories.setCustomStoryPath("**/a_short.story");
		 embedder.embedderControls().useStoryTimeoutInSecsByPath("**/a_short.story:25,**/a_short.story:25").doFailOnStoryTimeout(true);
	   	 embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		 Map<String,Long> timeoutValuesUsed = embedder.storyManager.timeoutValuesUsed;
         assertThat(timeoutValuesUsed.get("a_short.story"), equalTo(new Long(25)));	  
         ThreadsStories.setCustomStoryPath(null);
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

			public String loadResourceAsText(String resourcePath) {
				return null;
			}

        }
    }

}
