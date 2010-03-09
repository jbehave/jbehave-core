package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.scenario.RunnableScenario;

import java.util.HashMap;
import java.util.Map;

/**
 * Mojo to run scenarios
 *
 * @author Mauro Talevi
 * @goal run-scenarios
 */
public class ScenarioRunnerMojo extends AbstractScenarioMojo {

    /**
     * The boolean flag to run in batch mode
     *
     * @parameter default-value="false"
     */
    private boolean batch;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipScenarios()) {
            getLog().info("Skipped running scenarios");
            return;
        }

        Map<String, Throwable> failedScenarios = new HashMap<String, Throwable>();
        for (RunnableScenario scenario : scenarios()) {
            String scenarioName = scenario.getClass().getName();
            try {
                getLog().info("Running scenario " + scenarioName);
                scenario.runScenario();
            } catch (Throwable e) {
                String message = "Failure in running scenario " + scenarioName;
                if (batch) {
                    // collect and postpone decision to throw exception
                    failedScenarios.put(scenarioName, e);
                } else {
                    if (ignoreFailure()) {
                        getLog().warn(message, e);
                    } else {
                        throw new MojoExecutionException(message, e);
                    }
                }
            }
        }

        if (batch && failedScenarios.size() > 0) {
            String message = "Failure in runing scenarios: " + format(failedScenarios);
            if ( ignoreFailure() ){
                getLog().warn(message);
            } else {
                throw new MojoExecutionException(message);
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
    
}


