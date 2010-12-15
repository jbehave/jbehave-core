package org.jbehave.hudson;

import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;

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
        return "jbehave-3.2-to-junit-1.0.xsl";
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return new MavenSurefireModel();
    }
}
