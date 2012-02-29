package com.learnthinkcode.example.jbehave;

import org.jbehave.core.Embeddable;
import org.jbehave.core.io.StoryPathResolver;

public class ExplicitStoryPathResolver implements StoryPathResolver {

	private String storyPath;
	
	public ExplicitStoryPathResolver(String storyPath) {
		this.storyPath = storyPath;
	}
	
	@Override
	public String resolve(Class<? extends Embeddable> embeddableClass) {
		return storyPath;
	}

}
