package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.Composite;
import org.jbehave.core.parsers.CompositeParser;

/**
 * @author Valery Yatsynovich
 */
public class CompositeCandidateSteps extends AbstractCandidateSteps {

    private final Set<String> compositePaths;

    public CompositeCandidateSteps(Configuration configuration, Set<String> compositePaths) {
        super(configuration);
        this.compositePaths = compositePaths;
    }

    @Override
    public List<StepCandidate> listCandidates() {
        CompositeParser parser = configuration().compositeParser();
        ResourceLoader resourceLoader = configuration().storyLoader();
        List<StepCandidate> candidates = new ArrayList<>();
        for (String compositePath : compositePaths) {
            List<Composite> composites = parser.parseComposites(resourceLoader.loadResourceAsText(compositePath));
            addCandidatesFromComposites(candidates, composites);
        }
        return candidates;
    }

    private void addCandidatesFromComposites(List<StepCandidate> candidates, List<Composite> composites) {
        for (Composite composite : composites) {
            String[] steps = composite.getSteps().toArray(new String[0]);
            addCandidatesFromVariants(candidates, composite.getStepType(), composite.getStepWithoutStartingWord(),
                    composite.getPriority(), steps);
        }
    }

    private void addCandidatesFromVariants(List<StepCandidate> candidates, StepType stepType, String value,
            int priority, String[] steps) {
        PatternVariantBuilder b = new PatternVariantBuilder(value);
        for (String variant : b.allVariants()) {
            StepCandidate candidate = createCandidate(variant, priority, stepType, null, null, null);
            checkForDuplicateCandidates(candidates, candidate);
            candidate.composedOf(steps);
            candidates.add(candidate);
        }
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeStories() {
        return Collections.emptyList();
    }

    @Override
    public List<BeforeOrAfterStep> listAfterStories() {
        return Collections.emptyList();
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeStory(boolean givenStory) {
        return Collections.emptyList();
    }

    @Override
    public List<BeforeOrAfterStep> listAfterStory(boolean givenStory) {
        return Collections.emptyList();
    }

    @Override
    public Map<ScenarioType, List<BeforeOrAfterStep>> listBeforeScenario() {
        return Collections.emptyMap();
    }

    @Override
    public Map<ScenarioType, List<BeforeOrAfterStep>> listAfterScenario() {
        return Collections.emptyMap();
    }
}
