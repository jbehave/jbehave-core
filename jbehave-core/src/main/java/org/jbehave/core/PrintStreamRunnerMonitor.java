package org.jbehave.core;

import java.io.PrintStream;

public class PrintStreamRunnerMonitor implements StoryRunnerMonitor {
    private PrintStream output;

    public PrintStreamRunnerMonitor() {
        this(System.out);
    }

    public PrintStreamRunnerMonitor(PrintStream output) {
        this.output = output;
    }

    public void storiesBatchFailed(String failedStories) {
        output.println("Failed to run batch stories "+ failedStories);
    }

    public void storyFailed(String storyName, Throwable e) {
        output.println("Failed to run story "+storyName);
        e.printStackTrace(output);
    }

    public void runningStory(String storyName) {
        output.println("Running story "+storyName);
    }

    public void storiesNotRun() {
        output.println("Stories not run");
    }
}
