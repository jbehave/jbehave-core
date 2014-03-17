package org.jbehave.core.io;

public interface StoryLoader extends ResourceLoader {

    String loadStoryAsText(String storyPath);

}
