package org.jbehave.core.steps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

/**
 * An {@link InjectableStepsFactory} that scans for classes in the classpath.
 * The factory allows the package names to scan to be specified, or the root
 * class from which the package name is derived.
 * All classes that include any method annotation will be collected in the scan.
 */
public class ScanningStepsFactory extends AbstractStepsFactory {

	private final Set<Class<?>> types = new HashSet<Class<?>>();

	public ScanningStepsFactory(Configuration configuration, Class<?> root) {
		this(configuration, root.getPackage().getName());
	}

	public ScanningStepsFactory(Configuration configuration,
			String... packageNames) {
		super(configuration);
		for (String packageName : packageNames) {
			types.addAll(scanTypes(packageName));
		}
	}

	private Set<Class<?>> scanTypes(String packageName) {
		Reflections reflections = new Reflections(packageName,
				new MethodAnnotationsScanner());
		Set<Class<?>> types = new HashSet<Class<?>>();
		types.addAll(typesAnnotatedWith(reflections, Given.class));
		types.addAll(typesAnnotatedWith(reflections, When.class));
		types.addAll(typesAnnotatedWith(reflections, Then.class));
		types.addAll(typesAnnotatedWith(reflections, Before.class));
		types.addAll(typesAnnotatedWith(reflections, After.class));
		types.addAll(typesAnnotatedWith(reflections, BeforeScenario.class));
		types.addAll(typesAnnotatedWith(reflections, AfterScenario.class));
		types.addAll(typesAnnotatedWith(reflections, BeforeStory.class));
		types.addAll(typesAnnotatedWith(reflections, AfterStory.class));
		types.addAll(typesAnnotatedWith(reflections, BeforeStories.class));
		types.addAll(typesAnnotatedWith(reflections, AfterStories.class));
		return types;
	}

	private Set<Class<?>> typesAnnotatedWith(Reflections reflections,
			Class<? extends Annotation> annotation) {
		Set<Class<?>> types = new HashSet<Class<?>>();
		Set<Method> methodsAnnotatedWith = reflections
				.getMethodsAnnotatedWith(annotation);
		for (Method method : methodsAnnotatedWith) {
			types.add(method.getDeclaringClass());
		}
		return types;
	}

	@Override
	protected List<Class<?>> stepsTypes() {
		return new ArrayList<Class<?>>(types);
	}

	public Object createInstanceOfType(Class<?> type) {
		Object instance;
		try {
			instance = type.newInstance();
		} catch (Exception e) {
			throw new StepsInstanceNotFound(type, this);
		}
		return instance;
	}

}
