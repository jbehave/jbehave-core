package com.lunivore.gameoflife.steps;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.StoryReporter;


public class GridStory extends JUnitStory {

	public GridStory() {
		useConfiguration(new MostUsefulStoryConfiguration() {
			@Override
			public StoryPathResolver storyPathResolver() {
				// Default story path extension is ".story", but here were are using no extension
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
				return new ConsoleOutput();
			}
		});
		addSteps(new GridSteps());
	}
}
