package org.jbehave.core.embedder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holds flags used by the StoryRunner to control story execution flow.
 */
public class StoryControls {

    private boolean dryRun = false;
    private boolean parametriseGivenStoriesByExamples = false;
    private boolean resetStateBeforeStory = true;
    private boolean resetStateBeforeScenario = true;
    private boolean skipScenariosAfterFailure = false;
    private boolean skipBeforeAndAfterScenarioStepsIfGivenStory = false;
    private String storyMetaPrefix = "";
    private String scenarioMetaPrefix = "";

    public StoryControls() {
    }

    public boolean dryRun() {
        return dryRun;
    }

    public boolean parametriseGivenStoriesByExamples() {
        return parametriseGivenStoriesByExamples;
    }

    public boolean resetStateBeforeStory() {
        return resetStateBeforeStory;
    }

    public boolean resetStateBeforeScenario() {
        return resetStateBeforeScenario;
    }

    public boolean skipScenariosAfterFailure() {
        return skipScenariosAfterFailure;
    }

    public boolean skipBeforeAndAfterScenarioStepsIfGivenStory() {
        return skipBeforeAndAfterScenarioStepsIfGivenStory;
    }

    public String storyMetaPrefix() {
        return storyMetaPrefix;
    }

    public String scenarioMetaPrefix() {
        return scenarioMetaPrefix;
    }

    public StoryControls doDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    public StoryControls doParametriseGivenStoriesByExamples(boolean parametriseGivenStoriesByExamples) {
        this.parametriseGivenStoriesByExamples = parametriseGivenStoriesByExamples;
        return this;
    }
    
    public StoryControls doResetStateBeforeScenario(boolean resetStateBeforeScenario) {
        this.resetStateBeforeScenario = resetStateBeforeScenario;
        return this;
    }
    
    public StoryControls doResetStateBeforeStory(boolean resetStateBeforeStory) {
        this.resetStateBeforeStory = resetStateBeforeStory;
        return this;
    }
    
    public StoryControls doSkipScenariosAfterFailure(boolean skipScenariosAfterFailure) {
        this.skipScenariosAfterFailure = skipScenariosAfterFailure;
        return this;
    }

    public StoryControls doSkipBeforeAndAfterScenarioStepsIfGivenStory(boolean skipBeforeAndAfterScenarioStepsIfGivenStory) {
        this.skipBeforeAndAfterScenarioStepsIfGivenStory = skipBeforeAndAfterScenarioStepsIfGivenStory;
        return this;
    }

    public StoryControls useStoryMetaPrefix(String storyMetaPrefix){
        this.storyMetaPrefix = storyMetaPrefix;
        return this;        
    }

    public StoryControls useScenarioMetaPrefix(String scenarioMetaPrefix){
        this.scenarioMetaPrefix = scenarioMetaPrefix;
        return this;        
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
