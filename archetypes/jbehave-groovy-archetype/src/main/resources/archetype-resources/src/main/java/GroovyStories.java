#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.ParanamerConfiguration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitReportingRunner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;
import org.junit.runner.RunWith;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

/**
 * <p>
 * {@link Embeddable} class to run multiple textual stories using steps written in Groovy.
 * </p>
 */
@RunWith(JUnitReportingRunner.class)
public class GroovyStories extends JUnitStories {

    @Override
    public Configuration configuration() {
        return new ParanamerConfiguration()
                .useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats().withFormats(CONSOLE, HTML));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new GroovyStepsFactory(configuration(), new GroovyContext());
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
    }

}
