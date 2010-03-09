package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

import org.apache.tools.ant.BuildException;
import org.jbehave.scenario.RunnableScenario;

/**
 * Ant task that generate stepdocs
 * 
 * @author Mauro Talevi
 */
public class StepdocTask extends AbstractScenarioTask {

    public void execute() throws BuildException {
        if (skipScenarios()) {
            log("Skipped running scenarios", MSG_INFO);
            return;
        }
        for (RunnableScenario scenario : scenarios()) {
            String scenarioName = scenario.getClass().getName();
            try {
                log("Generating stepdoc for " + scenarioName);
                scenario.generateStepdoc();
            } catch (Throwable e) {
                String message = "Failed to generate stepdoc for " + scenarioName;
                if (ignoreFailure()) {
                    log(message, e, MSG_WARN);
                } else {
                    throw new BuildException(message, e);
                }
            }
        }
    }

}
