package org.jbehave.core.steps.needle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.needle.configuration.CollectInjectionProvidersFromStepsInstance;
import org.jbehave.core.steps.needle.configuration.CreateInstanceByDefaultConstructor;
import org.jbehave.core.steps.needle.configuration.JBehaveNeedleConfiguration;
import org.needle4j.NeedleTestcase;
import org.needle4j.injection.InjectionProvider;
import org.needle4j.reflection.ReflectionUtil;

/**
 * An {@link InjectableStepsFactory} that uses a Needle {@link InjectionProvider} for the composition and instantiation
 * of all components that contain JBehave annotated methods.
 * @author Simon Zambrovski (simon.zambrovski@holisticon.de)
 * @author Jan Galinski (jan.galinski@holisticon.de)
 */
public class NeedleStepsFactory extends NeedleTestcase implements InjectableStepsFactory {

    private final Map<Class<?>, Object> cachedTypeInstances = new LinkedHashMap<Class<?>, Object>();

    private Configuration configuration;
    private Class<?>[] steps;

    /**
     * Creates factory with given configuration and step instances.
     * @param configuration
     *        JBehave configuration
     * @param steps
     *        step classes
     */
    public NeedleStepsFactory(final Configuration configuration, final Class<?>... steps) {
        this(configuration, null, steps);
    }

    /**
     * Creates factory with given configuration, injection providers and step instances.
     * @param configuration
     *        JBehave configuration
     * @param injectionProviders
     *        injection providers.
     * @param steps
     *        step classes
     */
    public NeedleStepsFactory(final Configuration configuration, final Set<InjectionProvider<?>> injectionProviders, final Class<?>... steps) {
        super(setUpInjectionProviders(JBehaveNeedleConfiguration.RESOURCE_JBEHAVE_NEEDLE));
        if (injectionProviders != null) {
            addInjectionProvider(toArray(injectionProviders));
        }
        if (this.configuration == null) {
            this.configuration = new MostUsefulConfiguration();
        } else {
            this.configuration = configuration;
        }
        this.steps = steps;
    }

    /**
     * {@inheritDoc}
     */
    public List<CandidateSteps> createCandidateSteps() {
        final List<CandidateSteps> result = new ArrayList<CandidateSteps>();
        for (final Class<?> type : steps) {
            if (hasAnnotatedMethods(type)) {
                configuration.parameterConverters().addConverters(methodReturningConverters(type));
                result.add(new Steps(configuration, type, this));
            }
        }
        return result;
    }

    public Object createInstanceOfType(final Class<?> type) {
        final Object instance = cachedTypeInstances.get(type);
        if (instance == null) {
            try {
                final Object stepsInstance = createInstanceUsingNeedleTestCase(type);
                final InjectionProvider<?>[] foundProviders = CollectInjectionProvidersFromStepsInstance.INSTANCE.apply(stepsInstance);

                addInjectionProvider(foundProviders);

                initTestcase(stepsInstance);

                cachedTypeInstances.put(type, stepsInstance);
                return stepsInstance;
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return instance;
    }

    /**
     * Uses private instantiation methods of NeedleTestCase via {@link ReflectionUtil#invokeMethod(Object, String, Object...)}. First tries to create new
     * instance with constructor injection, then falls back to default constructor. If creation fails, an IllegalStateException is thrown.
     * @param type type of instance to create
     * @return new instance of type. Never <code>null</code>
     * @throws IllegalStateException when creation fails.
     */
    private Object createInstanceUsingNeedleTestCase(final Class<?> type) throws IllegalStateException {
        try {
            Object instance = ReflectionUtil.invokeMethod(this, "getInstanceByConstructorInjection", type);
            if (instance == null) {
                instance = CreateInstanceByDefaultConstructor.INSTANCE.apply(type);
            }
            if (instance == null) {
                throw new IllegalStateException("failed to create instance of type " + type.getCanonicalName());
            }
            return instance;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Create parameter converters from methods annotated with @AsParameterConverter
     * @see {@link AbstractStepsFactory}
     */
    private List<ParameterConverter> methodReturningConverters(final Class<?> type) {
        final List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (final Method method : type.getMethods()) {
            if (method.isAnnotationPresent(AsParameterConverter.class)) {
                converters.add(new MethodReturningConverter(method, type, this));
            }
        }
        return converters;
    }

    /**
     * Add injection providers.
     * @param providers
     *        add injection providers after factory construction.
     */
    public void addInjectionProviders(final Set<InjectionProvider<?>> providers) {
        if (providers != null) {
            addInjectionProvider(toArray(providers));
        }
    }

    /**
     * Determines if the given type is a {@link Class} containing at least one method annotated with annotations from
     * package "org.jbehave.core.annotations".
     * @param type
     *        the Type of the steps instance
     * @return A boolean, <code>true</code> if at least one annotated method is found.
     * @see {@link AbstractStepsFactory}
     */
    static boolean hasAnnotatedMethods(final Type type) {
        if (type instanceof Class<?>) {
            for (final Method method : ((Class<?>)type).getMethods()) {
                for (final Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().getName().startsWith("org.jbehave.core.annotations")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Read injection providers configuration from a resource.
     * @param resourceName
     *        resource name
     * @return injection providers.
     */
    static InjectionProvider<?>[] setUpInjectionProviders(final String resourceName) {
        return new JBehaveNeedleConfiguration(resourceName).getInjectionProviders();
    }

    /**
     * Set to array.
     * @param injectionProviders
     *        set of providers
     * @return array of providers
     */
    static InjectionProvider<?>[] toArray(final Set<InjectionProvider<?>> injectionProviders) {
        return injectionProviders.toArray(new InjectionProvider<?>[injectionProviders.size()]);
    }

}
