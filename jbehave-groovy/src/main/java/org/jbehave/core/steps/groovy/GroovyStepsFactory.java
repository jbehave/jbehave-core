package org.jbehave.core.steps.groovy;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.steps.AbstractStepsFactory;

public class GroovyStepsFactory extends AbstractStepsFactory {

    private final GroovyContext context;

    public GroovyStepsFactory(Configuration configuration, GroovyContext context){
        super(configuration);
        this.context = context;
    }

    @Override
    protected List<Object> stepsInstances() {
        List<Object> instances = new ArrayList<Object>();
        for (Object object : context.getInstances() ){
            if (hasAnnotatedMethods(object.getClass())) {
                instances.add(object);
            }
        }
        return instances;
    }

}