package org.jbehave.jenkins;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;

@SuppressWarnings("serial")
public class JBehaveType extends XUnitType {

    public JBehaveType(String pattern, boolean failedIfNotNew, boolean deleteJUnitFiles) {
        super(pattern, failedIfNotNew, deleteJUnitFiles);
    }

    @Override
    public TestTypeDescriptor<?> getDescriptor() {
        return null;
    }

    public Object readResolve() {
        return new JBehavePluginType(this.getPattern(), this.isFailIfNotNew(), this.isDeleteJUnitFiles(), this.isStopProcessingIfError());
    }

}
