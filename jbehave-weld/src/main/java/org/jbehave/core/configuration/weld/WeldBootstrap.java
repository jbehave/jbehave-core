package org.jbehave.core.configuration.weld;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.annotations.weld.WeldConfiguration;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.weld.WeldStepsFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

@ApplicationScoped
public class WeldBootstrap extends Weld {
    private WeldContainer weld;

    public WeldBootstrap() {
    }

    @Override
    public WeldContainer initialize() {
        weld = super.initialize();
        return weld;
    }

    public WeldAnnotationBuilder findAnnotationBuilder(Class<?> annotatedClass) {
        return weld.instance().select(InstanceManager.class).get().findBuilder(annotatedClass);
    }

    @ApplicationScoped
    public static class InstanceManager {

        @Inject
        @Any
        @UsingWeld
        private Instance<Object> instances;

        @Inject
        @WeldConfiguration
        private Configuration configuration;

        @Inject
        private WeldStepsFactory stepsFactory;

        private Map<Class<?>, WeldAnnotationBuilder> builders = null;

        public void build() {
            builders = new HashMap<>();
            for (Object o : instances) {
                Class<?> instanceClass = o.getClass();
                WeldAnnotationBuilder builder = new WeldAnnotationBuilder(instanceClass, configuration, stepsFactory);
                builders.put(instanceClass, builder);
            }
        }

        public WeldAnnotationBuilder findBuilder(Class<?> annotatedClass) {
            if (builders == null) {
                build();
            }

            if (builders.containsKey(annotatedClass)) {
                return builders.get(annotatedClass);
            } else {
                return new WeldAnnotationBuilder(annotatedClass, configuration, stepsFactory);
            }
        }

    }

}
