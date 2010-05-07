import com.lunivore.gameoflife.steps.GridSteps;
import org.jbehave.core.JUnitStory;
import org.jbehave.core.PropertyBasedStoryConfiguration;
import org.jbehave.core.parser.LoadFromRelativeFile;
import org.jbehave.core.parser.RegexStoryParser;
import org.jbehave.core.reporters.PrintStreamOutput;

public class ICanToggleACellFromDefaultPackage extends JUnitStory {

    public ICanToggleACellFromDefaultPackage() {
        useConfiguration(new PropertyBasedStoryConfiguration()
                .useStoryParser(new RegexStoryParser())
                .useStoryReporter(new PrintStreamOutput())
                .useStoryLoader(new LoadFromRelativeFile(this.getClass(), "../../src/main/java")));
        addSteps(new GridSteps());
    }
}
