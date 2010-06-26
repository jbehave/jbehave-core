package org.jbehave.core.io.stories;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.TxtOutput;

public class MyStory extends JUnitStory {

    public MyStory() {
        // Making sure this doesn't output to the build while it's running
        useConfiguration(new MostUsefulConfiguration() {
            @Override
            public StoryReporter defaultStoryReporter() {
                return new TxtOutput(new PrintStream(new ByteArrayOutputStream()));
            }
        });
    }
}