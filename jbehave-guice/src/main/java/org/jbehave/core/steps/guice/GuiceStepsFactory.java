package org.jbehave.core.steps.guice;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * An {@link InjectableStepsFactory} that uses a Guice {@link Injector} for the
 * composition and instantiation of all components that contain JBehave
 * annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class GuiceStepsFactory extends AbstractStepsFactory {

	private final Injector parent;

	public GuiceStepsFactory(Configuration configuration, Injector parent) {
		super(configuration);
		this.parent = parent;
	}

	@Override
	protected List<Object> stepsInstances() {
		List<Object> steps = new ArrayList<Object>();
		for (Binding<?> binding : parent.getBindings().values()) {
			Key<?> key = binding.getKey();
			if (isAnnotated(key.getTypeLiteral().getType())) {
				steps.add(parent.getInstance(key));
			}
		}
		return steps;
	}

}
