package org.jbehave.core.io.stories;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.TxtOutput;

public abstract class MyMultipleStory extends JUnitStory {
	public MyMultipleStory() {
        // Making sure this doesn't output to the build while it's running
        useConfiguration(new MostUsefulConfiguration()
        		.useStoryReporterBuilder(new StoryReporterBuilder(){
                    @Override
                    public StoryReporter build(String storyPath) {
                        return new TxtOutput(new PrintStream(new ByteArrayOutputStream()));
                    }        		    
        		}));
    }
}
