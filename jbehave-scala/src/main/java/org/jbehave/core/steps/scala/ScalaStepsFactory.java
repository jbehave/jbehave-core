package org.jbehave.core.steps.scala;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.scala.ScalaContext;
import org.jbehave.core.steps.AbstractStepsFactory;

public class ScalaStepsFactory extends AbstractStepsFactory {

    private final ScalaContext context;

    public ScalaStepsFactory(Configuration configuration, ScalaContext context){
        super(configuration);
        this.context = context;
    }

    @Override
    protected List<Class<?>> stepsTypes() {
        List<Class<?>> types = new ArrayList<>();
        for (Object object : context.getInstances()) {
            if (hasAnnotatedMethods(object.getClass())) {
                types.add(object.getClass());
            }
        }
        return types;
    }

    @Override
    public Object createInstanceOfType(Class<?> type) {
        return context.getInstanceOfType(type);
    }

}
