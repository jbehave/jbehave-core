package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Queue;

import org.jbehave.core.model.StepPattern;

/**
 * Monitor which collects other {@link StepMonitor}s and delegates all invocations to the collected monitors.
 */
public class DelegatingStepMonitor implements StepMonitor {

    private final Collection<StepMonitor> delegates;

    /**
     * Creates DelegatingStepMonitor with a given collections of delegates
     *
     * @param delegates the StepMonitor-s to delegate to
     */
    public DelegatingStepMonitor(Collection<StepMonitor> delegates) {
        this.delegates = delegates;
    }

    /**
     * Creates DelegatingStepMonitor with a given varargs of delegates
     *
     * @param delegates the StepMonitor-s to delegate to
     */
    public DelegatingStepMonitor(StepMonitor... delegates) {
        this(asList(delegates));
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Queue<Class<?>> converterClasses) {
        for (StepMonitor monitor : delegates) {
            monitor.convertedValueOfType(value, type, converted, converterClasses);
        }
    }

    @Override
    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance) {
        for (StepMonitor monitor : delegates) {
            monitor.stepMatchesType(stepAsString, previousAsString, matchesType, stepType, method, stepsInstance);
        }
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method,
            Object stepsInstance) {
        for (StepMonitor monitor : delegates) {
            monitor.stepMatchesPattern(step, matches, stepPattern, method, stepsInstance);
        }
    }

    @Override
    public void foundParameter(String parameter, int position) {
        for (StepMonitor monitor : delegates) {
            monitor.foundParameter(parameter, position);
        }
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method) {
        for (StepMonitor monitor : delegates) {
            monitor.beforePerforming(step, dryRun, method);
        }
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method) {
        for (StepMonitor monitor : delegates) {
            monitor.afterPerforming(step, dryRun, method);
        }
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position) {
        for (StepMonitor monitor : delegates) {
            monitor.usingAnnotatedNameForParameter(name, position);
        }
    }

    @Override
    public void usingNaturalOrderForParameter(int position) {
        for (StepMonitor monitor : delegates) {
            monitor.usingNaturalOrderForParameter(position);
        }
    }

    @Override
    public void usingParameterNameForParameter(String name, int position) {
        for (StepMonitor monitor : delegates) {
            monitor.usingParameterNameForParameter(name, position);
        }
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position) {
        for (StepMonitor monitor : delegates) {
            monitor.usingTableAnnotatedNameForParameter(name, position);
        }
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position) {
        for (StepMonitor monitor : delegates) {
            monitor.usingTableParameterNameForParameter(name, position);
        }
    }

    @Override
    public void usingStepsContextParameter(String parameter) {
        for (StepMonitor monitor : delegates) {
            monitor.usingStepsContextParameter(parameter);
        }
    }
}
