package org.jbehave.scenario.parser;

import org.jbehave.scenario.RunnableScenario;
import org.jbehave.scenario.definition.StoryDefinition;

/**
 * <p>
 * Loads scenarios contained in a story from a given scenario class.
 * </p>
 */
public interface ScenarioDefiner {

    StoryDefinition loadScenarioDefinitionsFor(Class<? extends RunnableScenario> scenarioClass);

    StoryDefinition loadScenarioDefinitionsFor(String scenarioPath);

}
