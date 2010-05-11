package com.lunivore.gameoflife.steps;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.MostUsefulStoryConfiguration;
import org.jbehave.core.parser.LoadFromClasspath;
import org.jbehave.core.parser.RegexStoryParser;
import org.jbehave.core.parser.StoryLoader;
import org.jbehave.core.parser.StoryParser;
import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.reporters.StoryReporter;


public class GridStory extends JUnitStory {

	public GridStory() {
		useConfiguration(new MostUsefulStoryConfiguration() {
			@Override
			public StoryPathResolver storyPathResolver() {
				return new UnderscoredCamelCaseResolver("");
			}

			@Override
			public StoryLoader storyLoader(){
				return new LoadFromClasspath(this.getClass().getClassLoader());
			}
			
			@Override
			public StoryParser storyParser() {
				return new RegexStoryParser(keywords());
			}

			@Override
			public StoryReporter storyReporter() {
				return new PrintStreamOutput();
			}
		});
		addSteps(new GridSteps());
	}
}
