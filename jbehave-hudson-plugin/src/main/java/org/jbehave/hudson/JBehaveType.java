package org.jbehave.hudson;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;

@SuppressWarnings("serial")
public class JBehaveType extends XUnitType {

    public JBehaveType(String pattern, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
        super(pattern, faildedIfNotNew, deleteJUnitFiles);
    }

    @Override
    public TestTypeDescriptor<?> getDescriptor() {
        return null;
    }

    public Object readResolve() {
        return new JBehavePluginType(this.getPattern(), this.isFaildedIfNotNew(), this.isDeleteJUnitFiles());
    }

}
