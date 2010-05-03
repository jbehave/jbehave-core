package org.jbehave.core;


public interface StoryRunnerMonitor {

    void runningStory(String storyName);

    void storyFailed(String storyName, Throwable e);

    void storiesNotRun();

    void storiesBatchFailed(String failedStories);
    
}
