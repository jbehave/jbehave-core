package org.jbehave.core.reporters;

public class ReportsCount {

    private final int stories;
    private final int storiesNotAllowed;
    private final int scenarios;
    private final int scenariosFailed;
    private final int scenariosNotAllowed;

    public ReportsCount(int stories, int storiesNotAllowed, int scenarios, int scenariosFailed, int scenariosNotAllowed) {
        this.stories = stories;
        this.storiesNotAllowed = storiesNotAllowed;
        this.scenarios = scenarios;
        this.scenariosFailed = scenariosFailed;
        this.scenariosNotAllowed = scenariosNotAllowed;
    }

    public int getStories() {
        return stories;
    }

    public int getStoriesNotAllowed() {
        return storiesNotAllowed;
    }

    public int getScenarios() {
        return scenarios;
    }

    public int getScenariosFailed() {
        return scenariosFailed;
    }
    
    public int getScenariosNotAllowed() {
        return scenariosNotAllowed;
    }
        
}
