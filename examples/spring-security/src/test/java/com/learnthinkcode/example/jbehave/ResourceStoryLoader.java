package com.learnthinkcode.example.jbehave;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.StoryLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ResourceStoryLoader implements StoryLoader {

	private ResourceLoader resourceLoader;

	public ResourceStoryLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public String loadStoryAsText(String storyPath) {
		try {
			Resource resource = resourceLoader.getResource(storyPath);
			return IOUtils.toString(resource.getInputStream());
		} catch (Exception e) {
			throw new InvalidStoryResource(storyPath, e);
		}
	}

}
