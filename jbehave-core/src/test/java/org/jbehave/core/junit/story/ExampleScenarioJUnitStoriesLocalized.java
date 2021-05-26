package org.jbehave.core.junit.story;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.junit.runner.RunWith;

@RunWith(JUnit4StoryRunner.class)
public class ExampleScenarioJUnitStoriesLocalized extends ExampleScenarioJUnitStories {

    @Override
    public Configuration configuration() {
        return super.configuration().useKeywords(new LocalizedKeywords(Locale.GERMAN));
    }

    @Override
    public List<String> storyPaths() {
        return Collections.singletonList("org/jbehave/core/junit/story/Multiplication_de.story");
    }
}
