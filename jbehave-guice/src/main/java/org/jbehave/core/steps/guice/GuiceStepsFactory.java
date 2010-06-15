package org.jbehave.core.steps.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Steps;

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
public class GuiceStepsFactory implements InjectableStepsFactory {

	private final Configuration configuration;
	private final Injector parent;

	public GuiceStepsFactory(Configuration configuration, Injector parent) {
		this.configuration = configuration;
		this.parent = parent;
	}

	public List<CandidateSteps> createCandidateSteps() {
		List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
		for (Binding<?> binding : parent.getBindings().values()) {
			Key<?> key = binding.getKey();
			if (isAnnotated(key.getTypeLiteral().getType())) {
				steps.add(new Steps(configuration, parent.getInstance(key)));
			}
		}
		return steps;
	}

	private boolean isAnnotated(Type type) {
		if (type instanceof Class<?>) {
			for (Method method : ((Class<?>) type).getMethods()) {
				for (Annotation annotation : method.getAnnotations()) {
					if (annotation.annotationType().getName().startsWith(
							"org.jbehave.core.annotations")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
