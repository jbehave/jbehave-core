package org.jbehave.scenario.parser;

import static java.util.Arrays.asList;
import static org.jbehave.Ensure.ensureThat;

import java.io.IOException;
import java.io.InputStream;

import org.jbehave.scenario.errors.InvalidScenarioClassPathException;
import org.junit.Test;

public class ScenarioClassNameFinderBehaviour {

    @Test
    public void canListScenarioNames() {
        ScenarioClassNameFinder finder = new ScenarioClassNameFinder();
        ensureThat(finder.listScenarioClassNames(".", ".", asList("**/scenarios/*.java"), asList("")).size() > 0);
    }

    @Test
    public void canReturnEmptyListForInexistentBasedir() {
        ScenarioClassNameFinder finder = new ScenarioClassNameFinder();
        ensureThat(finder.listScenarioClassNames("/inexistent", null, asList(""), asList("")).size() == 0);
    }

    @Test(expected=InvalidScenarioClassPathException.class)
    public void cannotListScenarioNamesForPathsThatAreInvalid() {
        ScenarioClassNameFinder finder = new ScenarioClassNameFinder();
        finder.listScenarioClassNames(".", null, null, null);
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
