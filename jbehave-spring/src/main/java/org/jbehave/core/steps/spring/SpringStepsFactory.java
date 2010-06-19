package org.jbehave.core.steps.spring;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * An {@link InjectableStepsFactory} that uses Spring's
 * {@link ListableBeanFactory} for the composition and instantiation of all
 * components that contain JBehave annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class SpringStepsFactory extends AbstractStepsFactory {

	private final ListableBeanFactory parent;

	public SpringStepsFactory(Configuration configuration,
			ListableBeanFactory parent) {
		super(configuration);
		this.parent = parent;
	}

	@Override
	protected List<Object> stepsInstances() {
		List<Object> steps = new ArrayList<Object>();
		for (String name : parent.getBeanDefinitionNames()) {
			Object bean = parent.getBean(name);
			if (isAnnotated(bean.getClass())) {
				steps.add(bean);
			}
		}
		return steps;
	}

}
