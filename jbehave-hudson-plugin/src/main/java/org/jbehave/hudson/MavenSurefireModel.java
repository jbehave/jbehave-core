package org.jbehave.hudson;

import java.io.Serializable;

import com.thalesgroup.dtkit.metrics.model.AbstractOutputMetric;

@SuppressWarnings("serial")
public class MavenSurefireModel extends AbstractOutputMetric implements Serializable {

    public String getKey() {
        return "surefire";
    }

    public String getDescription() {
        return "MAVEN SUREFIRE OUTPUT FORMAT 1.0";
    }

    public String getVersion() {
        return "1.0";
    }

    public String[] getXsdNameList() {
        return null;
    }
}
