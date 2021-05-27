package org.jbehave.core.steps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;

/**
 * <p>
 * An abstract implementation of {@link InjectableStepsFactory} that is provided
 * by concrete subclasses Object instances which contain the candidate steps
 * methods. The Object instances are wrapped by {@link Steps}.
 * </p>
 * <p>
 * The object instances are also inspected for methods annotated by {@link AsParameterConverter}
 * and the {@link ParameterConverter} is configured accordingly.
 * </p>
 */
public abstract class AbstractStepsFactory implements InjectableStepsFactory {

    private final Configuration configuration;

    public AbstractStepsFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<CandidateSteps> createCandidateSteps() {
        List<Class<?>> types = stepsTypes();
        List<CandidateSteps> steps = new ArrayList<>();
        for (Class<?> type : types) {
            configuration.parameterConverters().addConverters(
                    methodReturningConverters(type));
            steps.add(new Steps(configuration, type, this));
        }
        Set<String> compositePaths = configuration.compositePaths();
        if (!compositePaths.isEmpty()) {
            steps.add(new CompositeCandidateSteps(configuration, compositePaths));
        }
        return steps;
    }

    protected abstract List<Class<?>> stepsTypes();

    /**
     * Create parameter converters from methods annotated with @AsParameterConverter
     */
    private List<ParameterConverter> methodReturningConverters(Class<?> type) {
        List<ParameterConverter> converters = new ArrayList<>();

        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(AsParameterConverter.class)) {
                converters.add(new MethodReturningConverter(method, type, this));
            }
        }

        return converters;
    }

    /**
     * Determines if the given type is a {@link Class} containing at least one method
     * annotated with annotations from package "org.jbehave.core.annotations".
     *
     * @param type the Type of the steps instance
     * @return A boolean, <code>true</code> if at least one annotated method is found.
     */
    protected boolean hasAnnotatedMethods(Type type) {
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

    @SuppressWarnings("serial")
    public static class StepsInstanceNotFound extends RuntimeException {

        public StepsInstanceNotFound(Class<?> type, InjectableStepsFactory stepsFactory) {
            super("Steps instance not found for type " + type + " in factory " + stepsFactory);
        }

    }
}
