package org.jbehave.core.parser.stories;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.reporters.StoryReporter;

public abstract class MyPendingStory extends JUnitStory {

    public MyPendingStory() {
        // Making sure this doesn't output to the build while it's running
        useConfiguration(new MostUsefulStoryConfiguration() {
            @Override
            public StoryReporter storyReporter() {
                return new PrintStreamOutput(new PrintStream(new ByteArrayOutputStream()));
            }
        });
    }
}