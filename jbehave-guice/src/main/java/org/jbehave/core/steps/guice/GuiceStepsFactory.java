package org.jbehave.core.steps.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.StepsConfiguration;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * A factory class for {@link CandidateSteps} that uses an {@link Injector}
 * for the composition and instantiation of all components that contains
 * core annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class GuiceStepsFactory {

    private final StepsConfiguration configuration;
    private final Injector parent;

    public GuiceStepsFactory(StepsConfiguration configuration, Injector parent) {
        this.configuration = configuration;
        this.parent = parent;
    }

    public CandidateSteps[] createCandidateSteps() {
        List<Steps> steps = new ArrayList<Steps>();
        for (Binding<?> binding : parent.getBindings().values()) {
            Key<?> key = binding.getKey();
            if (containsScenarioAnnotations(key.getTypeLiteral().getType())) {
                steps.add(new Steps(configuration, parent.getInstance(key)));
            }
        }
        return steps.toArray(new CandidateSteps[steps.size()]);
    }

    private boolean containsScenarioAnnotations(Type type) {
        if ( type instanceof Class<?> ){
            for (Method method : ((Class<?>)type).getMethods()) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().getName().startsWith("org.jbehave.core.annotations")) {
                        return true;
                    }
                }
            }            
        }
        return false;
    }

}
