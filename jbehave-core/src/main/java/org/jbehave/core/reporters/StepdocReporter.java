package org.jbehave.core.reporters;

import java.util.List;

import org.jbehave.core.steps.Stepdoc;

public interface StepdocReporter {

    void stepdocs(List<Stepdoc> stepdocs, List<Object> stepsInstances);

    void stepdocsMatching(String stepAsString, List<Stepdoc> matching, List<Object> stepsIntances);

}
