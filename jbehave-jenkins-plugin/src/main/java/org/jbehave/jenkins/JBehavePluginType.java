package org.jbehave.jenkins;

import hudson.Extension;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("serial")
public class JBehavePluginType extends TestType {

    // @DataBoundConstructor
    public JBehavePluginType(final String pattern, final boolean failedIfNotNew, final boolean deleteOutputFiles) {
        this(pattern, failedIfNotNew, deleteOutputFiles, true);
    }

    @DataBoundConstructor
    public JBehavePluginType(final String pattern, final boolean failedIfNotNew, final boolean deleteOutputFiles, final boolean stopProcessingIfError) {
        super(pattern, failedIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Override
    public TestTypeDescriptor<?> getDescriptor() {
        return new JBehavePluginType.DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends TestTypeDescriptor<JBehavePluginType> {

        public DescriptorImpl() {
            super(JBehavePluginType.class, JBehaveInputMetric.class);
        }

        @Override
        public String getId() {
            return JBehavePluginType.class.getCanonicalName();
        }
    }

}
