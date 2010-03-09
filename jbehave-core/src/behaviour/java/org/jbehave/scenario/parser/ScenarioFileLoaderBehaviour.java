package org.jbehave.scenario.parser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.errors.InvalidScenarioResourceException;
import org.jbehave.scenario.errors.ScenarioNotFoundException;
import org.jbehave.scenario.i18n.I18nKeyWords;
import org.jbehave.scenario.parser.scenarios.MyPendingScenario;
import org.junit.Test;

public class ScenarioFileLoaderBehaviour {

    @Test
    public void canLoadScenario() {
        ScenarioParser parser = mock(ScenarioParser.class);
        ClasspathScenarioDefiner loader = new ClasspathScenarioDefiner(parser);
        loader.loadScenarioDefinitionsFor(MyPendingScenario.class);
        verify(parser).defineStoryFrom("Given my step", "org/jbehave/scenario/parser/scenarios/my_pending_scenario");
    }

    @Test
    public void canLoadScenarioWithCustomFilenameResolver() {
        ScenarioParser parser = mock(ScenarioParser.class);
        ClasspathScenarioDefiner loader = new ClasspathScenarioDefiner(new CasePreservingResolver(".txt"), parser);
        loader.loadScenarioDefinitionsFor(MyPendingScenario.class);
        verify(parser).defineStoryFrom("Given my step", "org/jbehave/scenario/parser/scenarios/MyPendingScenario.txt");
    }
    
    @Test(expected = ScenarioNotFoundException.class)
    public void cannotLoadScenarioForInexistentResource() {
        ClasspathScenarioDefiner loader = new ClasspathScenarioDefiner();
        loader.loadScenarioDefinitionsFor(InexistentScenario.class);
    }

    @Test(expected = InvalidScenarioResourceException.class)
    public void cannotLoadScenarioForInvalidResource() {
        ClasspathScenarioDefiner loader = new ClasspathScenarioDefiner(new UnderscoredCamelCaseResolver(), new PatternScenarioParser(new I18nKeyWords()), new InvalidClassLoader());
        loader.loadScenarioDefinitionsFor(MyPendingScenario.class);
    }

    static class InexistentScenario extends JUnitScenario {

    }

    static class InvalidClassLoader extends ClassLoader {

        @Override
        public InputStream getResourceAsStream(String name) {
            return new InputStream() {

                public int available() throws IOException {
                    return 1;
                }

                @Override
                public int read() throws IOException {
                    throw new IOException("invalid");
                }

            };
        }
    }
}
