package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.CompositeStep;
import org.jbehave.core.parsers.CompositeStepsParser;
import org.jbehave.core.parsers.RegexCompositeStepsParser;

/**
 * @author Valery Yatsynovich
 */
public class CompositeSteps extends AbstractCandidateSteps {

    private final List<String> compositeStepsDefinitionPaths;

    public CompositeSteps(Configuration configuration, List<String> compositeStepsDefinitionPaths) {
        super(configuration);
        this.compositeStepsDefinitionPaths = compositeStepsDefinitionPaths;
    }

    @Override
    public List<StepCandidate> listCandidates() {
        CompositeStepsParser parser = new RegexCompositeStepsParser(configuration().keywords());
        ResourceLoader resourceLoader = configuration().storyLoader();
        List<StepCandidate> candidates = new ArrayList<>();
        for (String compositeStepsDefinitionPath : compositeStepsDefinitionPaths) {
            String compositeStepsDefinition = resourceLoader.loadResourceAsText(compositeStepsDefinitionPath);
            List<CompositeStep> compositeSteps = parser.parseCompositeSteps(compositeStepsDefinition);
            addCandidatesFromCompositeSteps(candidates, compositeSteps);
        }
        return candidates;
    }

    private void addCandidatesFromCompositeSteps(List<StepCandidate> candidates, List<CompositeStep> compositeSteps) {
        for (CompositeStep compositeStep : compositeSteps) {
            String[] steps = compositeStep.getSteps().toArray(new String[0]);
            addCandidatesFromVariants(candidates, compositeStep.getStepType(),
                    compositeStep.getStepWithoutStartingWord(), steps);
        }
    }

    private void addCandidatesFromVariants(List<StepCandidate> candidates, StepType stepType, String value, String[] steps) {
        PatternVariantBuilder b = new PatternVariantBuilder(value);
        for (String variant : b.allVariants()) {
            checkForDuplicateCandidates(candidates, stepType, variant);
            StepCandidate candidate = createCandidate(variant, 0, stepType, null, null, null);
            candidate.composedOf(steps);
            candidates.add(candidate);
        }
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterStories() {
        return Collections.emptyList();
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterStory(boolean givenStory) {
        return Collections.emptyList();
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterScenario(ScenarioType type) {
        return Collections.emptyList();
    }
}
