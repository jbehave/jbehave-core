package org.jbehave.jenkins;

import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;

@SuppressWarnings("serial")
public class JBehaveInputMetric extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolName() {
        return "JBehave";
    }

    @Override
    public String getToolVersion() {
        return "3.x";
    }

    @Override
    public String getXslName() {
        return "jbehave-3.x-to-junit-1.0.xsl";
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return new MavenSurefireModel();
    }
}
