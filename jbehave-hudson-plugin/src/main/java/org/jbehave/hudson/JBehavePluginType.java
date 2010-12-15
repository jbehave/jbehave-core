package org.jbehave.hudson;

import org.kohsuke.stapler.DataBoundConstructor;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;

import hudson.Extension;

@SuppressWarnings("serial")
public class JBehavePluginType extends TestType {

    @DataBoundConstructor
    public JBehavePluginType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
    }

    public TestTypeDescriptor<?> getDescriptor() {
        return new JBehavePluginType.DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends TestTypeDescriptor<JBehavePluginType> {

        public DescriptorImpl() {
            super(JBehavePluginType.class, JBehaveInputMetric.class);
        }

        public String getId() {
            return JBehavePluginType.class.getCanonicalName();
        }
    }

}
