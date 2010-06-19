package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Finds candidate steps matching a textual step from a list of
 * {@link CandidateSteps} instances. It prioritises them by the
 * {@link PrioritisingStrategy} provided, defaulting to {@link ByPriorityField}.
 */
public class StepFinder {

    private PrioritisingStrategy prioritisingStrategy;

    public StepFinder() {
        this(new ByPriorityField());
    }

    public StepFinder(PrioritisingStrategy prioritisingStrategy) {
        this.prioritisingStrategy = prioritisingStrategy;
    }

    public List<Stepdoc> stepdocs(List<CandidateSteps> steps) {
        List<Stepdoc> stepdocs = new LinkedList<Stepdoc>();
        for (CandidateStep candidate : collectCandidates(steps)) {
            stepdocs.add(new Stepdoc(candidate));
        }
        return stepdocs;
    }

    public List<Stepdoc> findMatching(String stepAsString, List<CandidateSteps> candidateSteps) {
        List<Stepdoc> matching = new ArrayList<Stepdoc>();
        for (CandidateStep candidate : collectCandidates(candidateSteps)) {
            if (candidate.matches(stepAsString)) {
                matching.add(new Stepdoc(candidate));
            }
        }
        return matching;
    }

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
     * Collects a list of candidates for matching from {@link CandidateSteps}
     * instances.
     * 
     * @param candidateSteps
     *            the list {@link CandidateSteps} instances
     * @return A List of {@link CandidateStep}s
     */
    public List<CandidateStep> collectCandidates(List<CandidateSteps> candidateSteps) {
        List<CandidateStep> collected = new ArrayList<CandidateStep>();
        for (CandidateSteps candidates : candidateSteps) {
            collected.addAll(candidates.listCandidates());
        }
        return collected;
    }

    public List<CandidateStep> prioritise(String stepAsString, List<CandidateStep> candidateSteps) {
        return prioritisingStrategy.prioritise(stepAsString, candidateSteps);
    }

    /**
     * Strategy to priorise candidate steps by the
     * {@link CandidateStep#getPriority()} field which is settable in the
     * {@link Given}, {@link When}, {@link Then} annotations.
     */
    public static class ByPriorityField implements PrioritisingStrategy {

        public List<CandidateStep> prioritise(String stepAsString, List<CandidateStep> candidateSteps) {
            Collections.sort(candidateSteps, new Comparator<CandidateStep>() {
                public int compare(CandidateStep o1, CandidateStep o2) {
                    return o2.getPriority().compareTo(o1.getPriority());
                }
            });
            return candidateSteps;
        }

    }

    /**
     * Strategy to priorise candidate steps by Levenshtein Distance. C.f.
     * http://www.merriampark.com/ld.htm
     */
    public static class ByLevenshteinDistance implements PrioritisingStrategy {

        private LevenshteinDistance ld = new LevenshteinDistance();

        public List<CandidateStep> prioritise(final String stepAsString, List<CandidateStep> candidateSteps) {
            Collections.sort(candidateSteps, new Comparator<CandidateStep>() {
                public int compare(CandidateStep o1, CandidateStep o2) {
                    String scoringPattern1 = scoringPattern(o1);
                    String scoringPattern2 = scoringPattern(o2);
                    String stepWithoutStartingWord = trimStartingWord(stepAsString);
                    Integer score1 = 0 - ld.calculate(scoringPattern1, stepWithoutStartingWord);
                    Integer score2 = 0 - ld.calculate(scoringPattern2, stepWithoutStartingWord);
                    int result = score2.compareTo(score1);
                    // default to strategy by priority if no score result
                    return result != 0 ? result : o2.getPriority().compareTo(o1.getPriority());
                }

                private String scoringPattern(CandidateStep candidateStep) {
                    return candidateStep.getPatternAsString().replaceAll("\\s\\$\\w+\\s", " ")
                            .replaceAll("\\$\\w+", "");
                }

                private String trimStartingWord(String stepAsString) {
                    return StringUtils.substringAfter(stepAsString, " ");
                }

            });
            return candidateSteps;
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
