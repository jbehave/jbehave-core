package org.jbehave.core.configuration.cdi;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jbehave.core.annotations.cdi.CDIConfiguration;
import org.jbehave.core.annotations.cdi.UsingCDI;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.cdi.CDIStepsFactory;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CDIBootstrap extends Weld
{
    private static final Logger LOG = LoggerFactory.getLogger(CDIBootstrap.class);
    
    private WeldContainer weld;
    
    public CDIBootstrap() {}
    
    @Override
    protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap)
    {
        Deployment deployment = super.createDeployment(resourceLoader, bootstrap);
        return deployment;
    }
    
    @Override
    public WeldContainer initialize() {
        weld = super.initialize();
        return weld;
    }

    public CDIAnnotationBuilder findCDIAnnotationBuilder(Class<?> annotatedClass) {
        return weld.instance().select(InstanceManager.class).get().findBuilder(annotatedClass);
    }
    
    @ApplicationScoped
    public static class InstanceManager {
        
        @Inject @UsingCDI @Any
        private Instance<Object> instances;
        
        @Inject @CDIConfiguration
        Configuration configuration;
        
        @Inject CDIStepsFactory stepsFactory;
        
        Map<Class<?>, CDIAnnotationBuilder> builders = null;
        
        public void build() {
            builders = new HashMap<Class<?>,CDIAnnotationBuilder>();
            for(Object o:instances) {
                Class<?> instanceClass = o.getClass();
                LOG.debug("annotatedClass:" +instanceClass.getName());
                CDIAnnotationBuilder builder = new CDIAnnotationBuilder(instanceClass,configuration,stepsFactory);
                builders.put(instanceClass, builder);                
            }
        }
        
        public CDIAnnotationBuilder findBuilder(Class<?> annotatedClass) {
            if(builders == null) {
                build();
            }
            
            if(builders.containsKey(annotatedClass)) {
                return builders.get(annotatedClass);
            } else {
                return new CDIAnnotationBuilder(annotatedClass,configuration,stepsFactory);
            }
        }
        
    }

}
