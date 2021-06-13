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
import org.reflections.ReflectionsException;
import org.reflections.scanners.MethodAnnotationsScanner;

/**
 * An {@link InjectableStepsFactory} that scans for classes in the classpath.
 * The constructors allows the specification of the package names to scan or the
 * root class from which the package name is derived. All classes that include
 * any step method annotation ({@link Given}, {@link When}, {@link Then},
 * {@link Before}, {@link After}, etc ... ) will be collected in the scan.
 * Additional regex filters on the class names are provided via the
 * {@link #matchingNames(String)} and {@link #notMatchingNames(String)} methods,
 * which by default match all names.
 */
public class ScanningStepsFactory extends AbstractStepsFactory {

    private final Set<Class<?>> types = new HashSet<>();
    private String matchingRegex = ".*";
    private String notMatchingRegex = "";

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

    public ScanningStepsFactory matchingNames(String matchingRegex) {
        this.matchingRegex = matchingRegex;
        return this;
    }

    public ScanningStepsFactory notMatchingNames(String notMatchingRegex) {
        this.notMatchingRegex = notMatchingRegex;
        return this;
    }

    private Set<Class<?>> scanTypes(String packageName) {
        Reflections reflections = new Reflections(packageName,
                new MethodAnnotationsScanner());
        Set<Class<?>> types = new HashSet<>();
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
        Set<Class<?>> types = new HashSet<>();
        try {
            Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(annotation);
            for (Method method : methodsAnnotatedWith) {
                types.add(method.getDeclaringClass());
            }
        }
        catch (ReflectionsException e) {
            // https://github.com/ronmamo/reflections/issues/297
            if (!"Scanner MethodAnnotationsScanner was not configured".equals(e.getMessage())) {
                throw e;
            }
        }
        return types;
    }

    @Override
    protected List<Class<?>> stepsTypes() {
        List<Class<?>> matchingTypes = new ArrayList<>();
        for (Class<?> type : types) {
            String name = type.getName();
            if (name.matches(matchingRegex) && !name.matches(notMatchingRegex)) {
                matchingTypes.add(type);
            }
        }
        return matchingTypes;
    }

    @Override
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
