package com.lunivore.noughtsandcrosses.stories;

import java.net.URL;
import java.util.Locale;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;

import com.lunivore.noughtsandcrosses.NoughtsAndCrossesStory;
import com.lunivore.noughtsandcrosses.steps.BeforeAndAfterSteps;
import com.lunivore.noughtsandcrosses.steps.LolCatzSteps;
import com.lunivore.noughtsandcrosses.ui.WindowControl;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.TXT;

/**
 * Checks that we can support scenarios written in other languages,
 * eg: lolcatz
 */
public class PlayersCanHazTurns extends NoughtsAndCrossesStory {
 
    public PlayersCanHazTurns() {        
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        Keywords keywords = new LocalizedKeywords(new Locale("lc"),
                "i18n/keywords", classLoader);
        Configuration configuration = new MostUsefulConfiguration()
            .useKeywords(keywords)
            .useStoryParser(new RegexStoryParser(keywords, new TableTransformers()))
            .useStoryPathResolver(new UnderscoredCamelCaseResolver(""))
            .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(codeLocation)
                    .withDefaultFormats()
                    .withFormats(CONSOLE, TXT)
                    .withFailureTrace(true)
                    .withKeywords(keywords));
        useConfiguration(configuration);
        WindowControl windowControl = new WindowControl();        
        addSteps(new InstanceStepsFactory(configuration, new LolCatzSteps(windowControl), new BeforeAndAfterSteps(windowControl)).createCandidateSteps());
     }


}

