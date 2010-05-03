import com.lunivore.gameoflife.steps.GridSteps;
import org.jbehave.core.JUnitStory;
import org.jbehave.core.PropertyBasedStoryConfiguration;
import org.jbehave.core.parser.RegexStoryParser;
import org.jbehave.core.parser.StoryParser;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.reporters.StoryReporter;

public class ICanToggleACellFromDefaultPackage extends JUnitStory {

    public ICanToggleACellFromDefaultPackage() {     
        useConfiguration(new PropertyBasedStoryConfiguration() {
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
