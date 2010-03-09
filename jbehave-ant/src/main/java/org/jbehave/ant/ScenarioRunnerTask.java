package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.jbehave.scenario.RunnableScenario;

/**
 * Ant task that runs scenarios
 * 
 * @author Mauro Talevi
 */
public class ScenarioRunnerTask extends AbstractScenarioTask {

    /**
     * The boolean flag to run in batch mode
     */
    private boolean batch;

    public void execute() throws BuildException {
        if (skipScenarios()) {
            log("Skipped running scenarios", MSG_INFO);
            return;
        }

        Map<String, Throwable> failedScenarios = new HashMap<String, Throwable>();
        for (RunnableScenario scenario : scenarios()) {
            String scenarioName = scenario.getClass().getName();
            try {
                log("Running scenario " + scenarioName);
                scenario.runScenario();
            } catch (Throwable e) {
                String message = "Failure in running scenario " + scenarioName;
                if (batch) {
                    // collect and postpone decision to throw exception
                    failedScenarios.put(scenarioName, e);
                } else {
                    if (ignoreFailure()) {
                        log(message, e, MSG_WARN);
                    } else {
                        throw new BuildException(message, e);
                    }
                }
            }
        }
        if (batch && failedScenarios.size() > 0) {
            String message = "Failure in runing scenarios: " + format(failedScenarios);
            if (ignoreFailure()) {
                log(message, MSG_WARN);
            } else {
                throw new BuildException(message);
            }
        }
    }

    private String format(Map<String, Throwable> failedScenarios) {
        StringBuffer sb = new StringBuffer();
        for (String scenarioName : failedScenarios.keySet()) {
            Throwable cause = failedScenarios.get(scenarioName);
            sb.append("\n");
            sb.append(scenarioName);
            sb.append(": ");
            sb.append(cause.getMessage());
        }
        return sb.toString();
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }
}
