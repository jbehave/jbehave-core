package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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

    private PrioritisingStrategy prioritisingStrategy;

    /**
     * Creates a StepFinder with a {@link ByPriorityField} strategy
     */
    public StepFinder() {
        this(new ByPriorityField());
    }

    /**
     * Creates a StepFinder with a custom strategy
     * 
     * @param prioritisingStrategy the PrioritisingStrategy
     */
    public StepFinder(PrioritisingStrategy prioritisingStrategy) {
        this.prioritisingStrategy = prioritisingStrategy;
    }

    /**
     * Returns the stepdocs for the candidates collected from the 
     * given {@link CandidateSteps}.
     * 
     * @param candidateSteps the List of CandidateSteps
     * @return The list of Stepdocs, one for each {@link CandidateStep}.
     */
    public List<Stepdoc> stepdocs(List<CandidateSteps> candidateSteps) {
        List<Stepdoc> stepdocs = new LinkedList<Stepdoc>();
        for (CandidateStep candidate : collectCandidates(candidateSteps)) {
            stepdocs.add(new Stepdoc(candidate));
        }
        return stepdocs;
    }

    /**
     * Finds matching steps, represented as {@link Stepdoc}s, for a 
     * given textual step and a list of {@link CandidateSteps}.
     * 
     * @param stepAsText the textual step
     * @param candidateSteps the List of CandidateSteps
     * @return The list of Stepdocs, one for each matched {@link CandidateStep}.
     */
    public List<Stepdoc> findMatching(String stepAsText, List<CandidateSteps> candidateSteps) {
        List<Stepdoc> matching = new ArrayList<Stepdoc>();
        for (CandidateStep candidate : collectCandidates(candidateSteps)) {
            if (candidate.matches(stepAsText)) {
                matching.add(new Stepdoc(candidate));
            }
        }
        return matching;
    }

    /**
     * Returns the steps POJO instances associated to CandidateSteps
     * 
     * @param candidateSteps the List of CandidateSteps
     * @return The List of steps instances
     */
    public List<Object> stepsInstances(List<CandidateSteps> candidateSteps) {
        List<Object> instances = new ArrayList<Object>();
        for (CandidateSteps steps : candidateSteps) {
            if (steps instanceof Steps) {
                instances.add(((Steps) steps).instance());
            }
        }
        return instances;
    }

    /**
     * Collects a list of step candidates from {@link CandidateSteps}
     * instances.
     * 
     * @param candidateSteps
     *            the list {@link CandidateSteps} instances
     * @return A List of {@link CandidateStep}
     */
    public List<CandidateStep> collectCandidates(List<CandidateSteps> candidateSteps) {
        List<CandidateStep> collected = new ArrayList<CandidateStep>();
        for (CandidateSteps steps : candidateSteps) {
            collected.addAll(steps.listCandidates());
        }
        return collected;
    }

    /**
     * Prioritises the list of step candidates that match a given step.
     * 
     * @param stepAsText the textual step to match
     * @param candidates the List of CandidateStep
     * @return The prioritised list according to the
     *         {@link PrioritisingStrategy}.
     */
    public List<CandidateStep> prioritise(String stepAsText, List<CandidateStep> candidates) {
        return prioritisingStrategy.prioritise(stepAsText, candidates);
    }

    /**
     * Defines the priorising strategy of step candidates
     */
    public static interface PrioritisingStrategy {

        List<CandidateStep> prioritise(String stepAsString, List<CandidateStep> candidates);

    }

    /**
     * Strategy to priorise step candidates by the
     * {@link CandidateStep#getPriority()} field which is settable in the
     * {@link Given}, {@link When}, {@link Then} annotations.
     */
    public static class ByPriorityField implements PrioritisingStrategy {

        public List<CandidateStep> prioritise(String stepAsText, List<CandidateStep> candidates) {
            Collections.sort(candidates, new Comparator<CandidateStep>() {
                public int compare(CandidateStep o1, CandidateStep o2) {
                    return o2.getPriority().compareTo(o1.getPriority());
                }
            });
            return candidates;
        }

    }

    /**
     * Strategy to priorise candidate steps by <a
     * href="http://www.merriampark.com/ld.htm">Levenshtein Distance</a>
     */
    public static class ByLevenshteinDistance implements PrioritisingStrategy {

        private LevenshteinDistance ld = new LevenshteinDistance();

        public List<CandidateStep> prioritise(final String stepAsText, List<CandidateStep> candidates) {
            Collections.sort(candidates, new Comparator<CandidateStep>() {
                public int compare(CandidateStep o1, CandidateStep o2) {
                    String scoringPattern1 = scoringPattern(o1);
                    String scoringPattern2 = scoringPattern(o2);
                    String stepWithoutStartingWord = trimStartingWord(stepAsText);
                    Integer score1 = 0 - ld.calculate(scoringPattern1, stepWithoutStartingWord);
                    Integer score2 = 0 - ld.calculate(scoringPattern2, stepWithoutStartingWord);
                    int result = score2.compareTo(score1);
                    // default to strategy by priority if no score result
                    return result != 0 ? result : o2.getPriority().compareTo(o1.getPriority());
                }

                private String scoringPattern(CandidateStep candidate) {
                    return candidate.getPatternAsString().replaceAll("\\s\\$\\w+\\s", " ")
                            .replaceAll("\\$\\w+", "");
                }

                private String trimStartingWord(String stepAsString) {
                    return StringUtils.substringAfter(stepAsString, " ");
                }

            });
            return candidates;
        }

        private class LevenshteinDistance {

            public int calculate(String s, String t) {
                int d[][]; // matrix
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
