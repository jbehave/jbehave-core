/**
 * 
 */
package org.jbehave.core.steps.cdi;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbehave.core.annotations.cdi.CDIConfiguration;
import org.jbehave.core.annotations.cdi.CDIStep;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;

/**
 * An {@link InjectableStepsFactory} that uses a CDI {@link Inject} for the
 * composition and instantiation of all components that contain JBehave
 * annotated methods.
 * 
 * @author aaronwalker
 *
 */
@Singleton
public class CDIStepsFactory extends AbstractStepsFactory {
    
    @Inject @CDIStep @Any Instance<Object> instances;

    @Inject
    public CDIStepsFactory(@CDIConfiguration Configuration configuration) {
        super(configuration);
    }

    /* (non-Javadoc)
     * @see org.jbehave.core.steps.AbstractStepsFactory#stepsInstances()
     */
    @Override
    protected List<Object> stepsInstances() {
        List<Object> steps = new ArrayList<Object>();
        for(Object o: instances) {
            steps.add(o);
        }
        return steps;
    }

}
