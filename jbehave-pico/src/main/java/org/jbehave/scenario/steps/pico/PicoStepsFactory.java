package org.jbehave.scenario.steps.pico;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.Steps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

/**
 * A factory class for {@link CandidateSteps} that uses a {@link PicoContainer}
 * for the composition and instantiation of all components that contains
 * scenario annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class PicoStepsFactory {

    private final StepsConfiguration configuration;
    private final PicoContainer parent;

    public PicoStepsFactory(StepsConfiguration configuration, PicoContainer parent) {
        this.configuration = configuration;
        this.parent = parent;
    }

    public CandidateSteps[] createCandidateSteps() {
        List<Steps> steps = new ArrayList<Steps>();
        for (ComponentAdapter<?> adapter : parent.getComponentAdapters()) {
            if (containsScenarioAnnotations(adapter.getComponentImplementation())) {
                steps.add(new Steps(configuration, parent.getComponent(adapter.getComponentKey())));
            }
        }
        return steps.toArray(new CandidateSteps[steps.size()]);
    }

    private boolean containsScenarioAnnotations(Class<?> componentClass) {
        for (Method method : componentClass.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().startsWith("org.jbehave.scenario.annotations")) {
                    return true;
                }
            }
        }
        return false;
    }

}
