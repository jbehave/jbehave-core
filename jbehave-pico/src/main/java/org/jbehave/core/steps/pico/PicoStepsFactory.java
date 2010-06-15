package org.jbehave.core.steps.pico;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Steps;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

/**
 * An {@link InjectableStepsFactory} that uses a {@link PicoContainer} for the
 * composition and instantiation of all components that contain JBehave
 * annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class PicoStepsFactory implements InjectableStepsFactory {

	private final Configuration configuration;
	private final PicoContainer parent;

	public PicoStepsFactory(Configuration configuration, PicoContainer parent) {
		this.configuration = configuration;
		this.parent = parent;
	}

	public List<CandidateSteps> createCandidateSteps() {
		List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
		for (ComponentAdapter<?> adapter : parent.getComponentAdapters()) {
			if (isAnnotated(adapter.getComponentImplementation())) {
				steps.add(new Steps(configuration, parent.getComponent(adapter
						.getComponentKey())));
			}
		}
		return steps;
	}

	private boolean isAnnotated(Class<?> componentClass) {
		for (Method method : componentClass.getMethods()) {
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType().getName().startsWith(
						"org.jbehave.core.annotations")) {
					return true;
				}
			}
		}
		return false;
	}

}
