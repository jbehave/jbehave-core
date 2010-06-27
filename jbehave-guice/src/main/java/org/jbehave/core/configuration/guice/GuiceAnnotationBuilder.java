package org.jbehave.core.configuration.guice;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MissingAnnotationException;
import org.jbehave.core.configuration.MostUsefulConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class GuiceAnnotationBuilder extends AnnotationBuilder {

	private Injector injector;

	public GuiceAnnotationBuilder(Class<?> targetClass) {
		super(targetClass);
	}

	public GuiceAnnotationBuilder(Class<?> targetClass,
			AnnotationMonitor annotationMonitor) {
		super(targetClass, annotationMonitor);
	}

	@SuppressWarnings("rawtypes")
	public Configuration buildConfiguration() throws MissingAnnotationException {

		if (getFinder().isAnnotationPresent(UsingGuice.class)) {
			if (getFinder().isAnnotationValuePresent(UsingGuice.class,
					"modules")) {
				List<Module> moduleList = new ArrayList<Module>();
				List<Class> moduleClasses = getFinder().getAnnotatedValues(
						UsingGuice.class, Class.class, "modules");
				if (moduleClasses != null) {
					for (Class<Module> module : moduleClasses) {
						try {
							moduleList.add(module.newInstance());
						} catch (Exception e) {
							getAnnotationMonitor().elementCreationFailed(
									module, e);
						}
					}
				}
				// injecting other modules
				getInjector().createChildInjector(Modules.combine(moduleList));
			}
			return getInjector().getInstance(Configuration.class);
		} else {
			getAnnotationMonitor().annotationNotFound(UsingGuice.class,
					getAnnotatedClass());
		}
		return super.buildConfiguration();
	}

	@Override
	protected <T> T instanceOf(Class<T> type, Class<T> ofClass) {

		return getInjector().getInstance(ofClass);

	}

	public Injector getInjector() {
		if (injector == null) {
			// If I want use guice I need at least one module, so lets create
			// one and binding the config create by normal interpretation of
			// annotations...

			// lets create the injector now
			injector = Guice.createInjector(new AbstractModule() {

				@Override
				protected void configure() {
					bind(Configuration.class).to(MostUsefulConfiguration.class);
				}
			});
		}
		return injector;
	}

}
