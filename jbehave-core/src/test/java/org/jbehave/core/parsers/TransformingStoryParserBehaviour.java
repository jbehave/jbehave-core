package org.jbehave.core.parsers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableTransformers;
import org.junit.Test;


public class TransformingStoryParserBehaviour {

    private static final String NL = "\n";

    @Test
    public void shouldTransformAndParseStory() {
        StoryParser delegate = new RegexStoryParser(new LoadFromClasspath(), new TableTransformers());
        StoryTransformer transformer = new StoryTransformer() {            
            @Override
            public String transform(String storyAsText) {
                return storyAsText.replaceAll(",", "|");
            }
        };
        StoryParser parser = new TransformingStoryParser(delegate, transformer);
        String storyAsText = "Scenario: a scenario " + NL +
                "Given a scenario Given" + NL +
                "When I parse it to When" + NL +
                "And I parse it to And" + NL +
                "!-- And ignore me too" + NL +
                "Then I should get steps Then" + NL +
                "Examples:" + NL +
                ",Given,When,Then,And," + NL +
                ",Dato che,Quando,Allora,E,";

        Story story = parser.parseStory(storyAsText);
        assertThat(story.getScenarios().get(0).getExamplesTable().getRowCount(), equalTo(1));
    }
}
