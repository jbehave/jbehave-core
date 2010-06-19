package org.jbehave.core.steps.pico;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
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
public class PicoStepsFactory extends AbstractStepsFactory {

	private final PicoContainer parent;

	public PicoStepsFactory(Configuration configuration, PicoContainer parent) {
		super(configuration);
		this.parent = parent;
	}

	@Override
	protected List<Object> stepsInstances() {
		List<Object> steps = new ArrayList<Object>();
		for (ComponentAdapter<?> adapter : parent.getComponentAdapters()) {
			if (isAnnotated(adapter.getComponentImplementation())) {
				steps.add(parent.getComponent(adapter
						.getComponentKey()));
			}
		}
		return steps;
	}
}
