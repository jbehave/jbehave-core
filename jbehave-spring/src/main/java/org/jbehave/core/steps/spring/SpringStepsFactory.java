package org.jbehave.core.steps.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Steps;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * An {@link InjectableStepsFactory} that uses Spring's
 * {@link ListableBeanFactory} for the composition and instantiation of all
 * components that contain JBehave annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class SpringStepsFactory implements InjectableStepsFactory {

	private final Configuration configuration;
	private final ListableBeanFactory parent;

	public SpringStepsFactory(Configuration configuration,
			ListableBeanFactory parent) {
		this.configuration = configuration;
		this.parent = parent;
	}

	public List<CandidateSteps> createCandidateSteps() {
		List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
		for (String name : parent.getBeanDefinitionNames()) {
			Object bean = parent.getBean(name);
			if (isAnnotated(bean.getClass())) {
				steps.add(new Steps(configuration, bean));
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
