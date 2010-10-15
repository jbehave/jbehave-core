package org.jbehave.core.reporters;

public class ReportsCount {

    private final int stories;
    private final int scenarios;
    private final int failedScenarios;

    public ReportsCount(int stories, int scenarios, int failedScenarios) {
        this.stories = stories;
        this.scenarios = scenarios;
        this.failedScenarios = failedScenarios;
    }

    public int getStories() {
        return stories;
    }

    public int getScenarios() {
        return scenarios;
    }

    public int getFailedScenarios() {
        return failedScenarios;
    }

}
