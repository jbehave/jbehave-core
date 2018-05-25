package org.jbehave.jenkins;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.plugins.xunit.types.JUnitType;

@SuppressWarnings("serial")
public class JBehaveType extends JUnitType {

    public JBehaveType(final String pattern, final boolean skipNoTestFiles, final boolean failIfNotNew, final boolean deleteOutputFiles, final boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Override
    public TestTypeDescriptor<?> getDescriptor() {
        return null;
    }

    @Override
    public Object readResolve() {
        return new JBehavePluginType(this.getPattern(), this.isSkipNoTestFiles(), this.isFailIfNotNew(),
                this.isDeleteOutputFiles(), this.isStopProcessingIfError());
    }

}
