package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.condition.ReflectionBasedStepConditionMatcher;
import org.jbehave.core.condition.StepConditionMatcher;
import org.jbehave.core.embedder.AllStepCandidates;

/**
 * <p>
 * StepFinder is reponsible for finding and prioritising step candidates or
 * finding steps instances from {@link CandidateSteps}, which are created using
 * an {@link InjectableStepsFactory}.
 * </p>
 * <p>
 * The {@link StepCandidate}s are responsible for the matching of a particular
 * textual step and are sometimes represented as {@link Stepdoc}s, each of which
 * is simply a facade documenting a candidate. The candidates can be prioritised
 * via an injectable {@link PrioritisingStrategy}, defaulting to
 * {@link ByPriorityField}. A more sophisticated strategy that can be used is
 * the {@link ByLevenshteinDistance}.
 * </p>
 */
public class StepFinder {

    private final PrioritisingStrategy prioritisingStrategy;
    private final StepConditionMatcher stepConditionMatcher;

    /**
     * Creates a StepFinder with a {@link ByPriorityField} strategy
     */
    public StepFinder() {
        this(new ByPriorityField());
    }

    /**
     * Creates a StepFinder with a custom strategy
     * 
     * @param prioritisingStrategy
     *            the PrioritisingStrategy
     */
    public StepFinder(PrioritisingStrategy prioritisingStrategy) {
        this.prioritisingStrategy = prioritisingStrategy;
        this.stepConditionMatcher = new ReflectionBasedStepConditionMatcher();
    }

    /**
     * Creates a StepFinder with a custom step condition matcher
     * 
     * @param stepConditionMatcher
     *            the StepConditionMatcher
     */
    public StepFinder(StepConditionMatcher stepConditionMatcher) {
        this.prioritisingStrategy = new ByPriorityField();
        this.stepConditionMatcher = stepConditionMatcher;
    }

    /**
     * Returns the stepdocs for the candidates collected from the given
     * {@link CandidateSteps}.
     * 
     * @param candidateSteps
     *            the List of CandidateSteps
     * @return The List of Stepdocs, one for each {@link StepCandidate}.
     */
    public List<Stepdoc> stepdocs(List<CandidateSteps> candidateSteps) {
        return createStepdocs(candidate -> true, candidateSteps);
    }

    /**
     * Finds matching steps, represented as {@link Stepdoc}s, for a given
     * textual step and a list of {@link CandidateSteps}.
     * 
     * @param stepAsText
     *            the textual step
     * @param candidateSteps
     *            the List of CandidateSteps
     * @return The list of Stepdocs, one for each matched {@link StepCandidate}.
     */
    public List<Stepdoc> findMatching(String stepAsText, List<CandidateSteps> candidateSteps) {
        return createStepdocs(candidate -> candidate.matches(stepAsText), candidateSteps);
    }

    private List<Stepdoc> createStepdocs(Predicate<StepCandidate> candidateFilter,
            List<CandidateSteps> candidateSteps) {
        return new AllStepCandidates(stepConditionMatcher, candidateSteps).getRegularSteps().stream()
                .filter(candidateFilter)
                .map(Stepdoc::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the steps instances associated to CandidateSteps
     * 
     * @param candidateSteps
     *            the List of CandidateSteps
     * @return The List of steps instances
     */
    public List<Object> stepsInstances(List<CandidateSteps> candidateSteps) {
        List<Object> instances = new ArrayList<>();
        for (CandidateSteps steps : candidateSteps) {
            if (steps instanceof Steps) {
                instances.add(((Steps) steps).instance());
            }
        }
        return instances;
    }

    /**
     * Prioritises the list of step candidates that match a given step.
     * 
     * @param stepAsText
     *            the textual step to match
     * @param candidates
     *            the List of StepCandidate
     * @return The prioritised list according to the
     *         {@link PrioritisingStrategy}.
     */
    public List<StepCandidate> prioritise(String stepAsText, List<StepCandidate> candidates) {
        return prioritisingStrategy.prioritise(stepAsText, candidates);
    }

    /**
     * Defines the priorising strategy of step candidates
     */
    public static interface PrioritisingStrategy {

        List<StepCandidate> prioritise(String stepAsString, List<StepCandidate> candidates);

    }

    /**
     * Strategy to priorise step candidates by the
     * {@link StepCandidate#getPriority()} field which is settable in the
     * {@link Given}, {@link When}, {@link Then} annotations.
     */
    public static class ByPriorityField implements PrioritisingStrategy {

        @Override
        public List<StepCandidate> prioritise(String stepAsText, List<StepCandidate> candidates) {
            Collections.sort(candidates, new Comparator<StepCandidate>() {
                @Override
                public int compare(StepCandidate o1, StepCandidate o2) {
                    return o2.getPriority().compareTo(o1.getPriority());
                }
            });
            return candidates;
        }

    }

    /**
     * Strategy to priorise candidate steps by <a
     * href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein Distance</a>
     */
    public static class ByLevenshteinDistance implements PrioritisingStrategy {

        private LevenshteinDistance ld = new LevenshteinDistance();

        @Override
        public List<StepCandidate> prioritise(final String stepAsText, List<StepCandidate> candidates) {
            Collections.sort(candidates, new Comparator<StepCandidate>() {
                @Override
                public int compare(StepCandidate o1, StepCandidate o2) {
                    String scoringPattern1 = scoringPattern(o1);
                    String scoringPattern2 = scoringPattern(o2);
                    String stepWithoutStartingWord = trimStartingWord(stepAsText);
                    Integer score1 = 0 - ld.calculate(scoringPattern1, stepWithoutStartingWord);
                    Integer score2 = 0 - ld.calculate(scoringPattern2, stepWithoutStartingWord);
                    int result = score2.compareTo(score1);
                    // default to strategy by priority if no score result
                    return result != 0 ? result : o2.getPriority().compareTo(o1.getPriority());
                }

                private String scoringPattern(StepCandidate candidate) {
                    return candidate.getPatternAsString().replaceAll("\\s\\$\\w+\\s", " ").replaceAll("\\$\\w + ", "");
                }

                private String trimStartingWord(String stepAsString) {
                    return StringUtils.substringAfter(stepAsString, " ");
                }

            });
            return candidates;
        }

        private class LevenshteinDistance {

            @SuppressWarnings("checkstyle:LocalVariableName")
            public int calculate(String s, String t) {
                int[][] d; // matrix
                int n; // length of s
                int m; // length of t
                int i; // iterates through s
                int j; // iterates through t
                char s_i; // ith character of s
                char t_j; // jth character of t
                int cost; // cost

                // Step 1
                n = s.length();
                m = t.length();
                if (n == 0) {
                    return m;
                }
                if (m == 0) {
                    return n;
                }
                d = new int[n + 1][m + 1];
                // Step 2
                for (i = 0; i <= n; i++) {
                    d[i][0] = i;
                }
                for (j = 0; j <= m; j++) {
                    d[0][j] = j;
                }
                // Step 3
                for (i = 1; i <= n; i++) {
                    s_i = s.charAt(i - 1);
                    // Step 4
                    for (j = 1; j <= m; j++) {
                        t_j = t.charAt(j - 1);
                        // Step 5
                        if (s_i == t_j) {
                            cost = 0;
                        } else {
                            cost = 1;
                        }
                        // Step 6
                        d[i][j] = minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
                    }
                }
                // Step 7
                return d[n][m];
            }

            private int minimum(int a, int b, int c) {
                int mi = a;
                if (b < mi) {
                    mi = b;
                }
                if (c < mi) {
                    mi = c;
                }
                return mi;
            }

        }

    }

}
