package org.jbehave.core.embedder;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds flags used by the StoryRunner to control story execution flow.
 */
public class StoryControls {

    private boolean dryRun = false;
    private boolean resetStateBeforeStory = true;
    private boolean resetStateBeforeScenario = true;
    private boolean skipScenariosAfterFailure = false;
    private boolean skipBeforeAndAfterScenarioStepsIfGivenStory = false;
	private boolean ignoreMetaFiltersIfGivenStory = false;
	private boolean metaByRow = false;
    private String storyMetaPrefix = "";
    private String scenarioMetaPrefix = "";
    private boolean skipStoryIfGivenStoryFailed = false;
    private final ThreadLocal<StoryControls> currentStoryControls = ThreadLocal
            .withInitial(() -> new StoryControls(this));

    public StoryControls(StoryControls storyControls) {
        dryRun = storyControls.dryRun;
        resetStateBeforeStory = storyControls.resetStateBeforeStory;
        resetStateBeforeScenario = storyControls.resetStateBeforeScenario;
        skipScenariosAfterFailure = storyControls.skipScenariosAfterFailure;
        skipBeforeAndAfterScenarioStepsIfGivenStory = storyControls.skipBeforeAndAfterScenarioStepsIfGivenStory;
        ignoreMetaFiltersIfGivenStory = storyControls.ignoreMetaFiltersIfGivenStory;
        metaByRow = storyControls.metaByRow;
        storyMetaPrefix = storyControls.storyMetaPrefix;
        scenarioMetaPrefix = storyControls.scenarioMetaPrefix;
        skipStoryIfGivenStoryFailed = storyControls.skipStoryIfGivenStoryFailed;
        // should not be used
        currentStoryControls.set(null);
    }

    public StoryControls() {
    }
    
    public StoryControls currentStoryControls() {
        return currentStoryControls.get();
    }

    public boolean dryRun() {
        return currentStoryControls().dryRun;
    }

    public boolean resetStateBeforeStory() {
        return currentStoryControls().resetStateBeforeStory;
    }

    public boolean resetStateBeforeScenario() {
        return currentStoryControls().resetStateBeforeScenario;
    }
    
    public void resetCurrentStoryControls() {
        currentStoryControls.set(new StoryControls(this));
    }

    public boolean skipScenariosAfterFailure() {
        return currentStoryControls().skipScenariosAfterFailure;
    }

    public boolean skipBeforeAndAfterScenarioStepsIfGivenStory() {
        return currentStoryControls().skipBeforeAndAfterScenarioStepsIfGivenStory;
    }

    public boolean ignoreMetaFiltersIfGivenStory() {
		return currentStoryControls().ignoreMetaFiltersIfGivenStory;
	}

    public boolean metaByRow() {
        return currentStoryControls().metaByRow;
    }

    public String storyMetaPrefix() {
        return currentStoryControls().storyMetaPrefix;
    }

    public String scenarioMetaPrefix() {
        return currentStoryControls().scenarioMetaPrefix;
    }

    public boolean skipStoryIfGivenStoryFailed() {
        return currentStoryControls().skipStoryIfGivenStoryFailed;
    }

    public StoryControls doDryRun(boolean dryRun) {
        this.dryRun = dryRun;
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

	public StoryControls doIgnoreMetaFiltersIfGivenStory(
			boolean ignoreMetaFiltersIfGivenStory) {
		this.ignoreMetaFiltersIfGivenStory = ignoreMetaFiltersIfGivenStory;
		return this;
	}

	public StoryControls doMetaByRow(boolean metaByRow) {
	    this.metaByRow = metaByRow;
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

    public StoryControls doSkipStoryIfGivenStoryFailed(boolean skipStoryIfGivenStoryFailed) {
        this.skipStoryIfGivenStoryFailed = skipStoryIfGivenStoryFailed;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
