package com.lunivore.noughtsandcrosses.stories;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;

import java.net.URL;
import java.util.Locale;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.i18n.Encoding;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;

import com.lunivore.noughtsandcrosses.steps.BeforeAndAfterSteps;
import com.lunivore.noughtsandcrosses.steps.LolCatzSteps;
import com.lunivore.noughtsandcrosses.util.OAndXUniverse;

/**
 * Checks that we can support scenarios written in other languages,
 * eg: lolcatz
 */
public class PlayersCanHazTurns extends JUnitStory {
 
    public PlayersCanHazTurns() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        Keywords keywords = new LocalizedKeywords(new Locale("lc"),
                "com/lunivore/noughtsandcrosses/util/keywords", classLoader, new Encoding());
        Configuration configuration = new MostUsefulConfiguration()
            .useKeywords(keywords)
            .useStoryParser(new RegexStoryParser(keywords))
            .useStoryPathResolver(new UnderscoredCamelCaseResolver(""))
            .useDefaultStoryReporter(new ConsoleOutput(keywords))
            .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(codeLocation)
                    .withDefaultFormats()
                    .withFormats(CONSOLE, TXT)
                    .withFailureTrace(true)
                    .withKeywords(keywords));
        useConfiguration(configuration);
        OAndXUniverse universe = new OAndXUniverse();
        addSteps(new InstanceStepsFactory(configuration, new LolCatzSteps(universe), new BeforeAndAfterSteps(universe)).createCandidateSteps());
     }


}

