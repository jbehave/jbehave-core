/**
 * 
 */
package org.jbehave.core.steps.weld;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbehave.core.annotations.weld.WeldConfiguration;
import org.jbehave.core.annotations.weld.WeldStep;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;

/**
 * An {@link InjectableStepsFactory} that uses a Weld {@link Inject} for the
 * composition and instantiation of all components that contain JBehave
 * annotated methods.
 * 
 * @author Aaron Walker
 */
@Singleton
public class WeldStepsFactory extends AbstractStepsFactory {

    @Inject
    @Any
    @WeldStep
    private Instance<Object> instances;

    @Inject
    public WeldStepsFactory(@WeldConfiguration Configuration configuration) {
        super(configuration);
    }

    @Override
    protected List<Object> stepsInstances() {
        List<Object> steps = new ArrayList<Object>();
        for (Object o : instances) {
            steps.add(o);
        }
        return steps;
    }

}
