package org.jbehave.core.io.stories;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.reporters.TxtOutput;

public abstract class MyMultipleStory extends JUnitStory {
	public MyMultipleStory() {
        // Making sure this doesn't output to the build while it's running
        useConfiguration(new MostUsefulConfiguration()
        		.useStoryReporter(new TxtOutput(new PrintStream(new ByteArrayOutputStream())))
        );
    }
}
