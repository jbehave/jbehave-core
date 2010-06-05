package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.errors.PendingError;
import org.jbehave.core.parsers.StepMatcher;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class StepCreator {

    public static final String PARAMETER_NAME_START = "<";
    public static final String PARAMETER_NAME_END = ">";
    public static final String PARAMETER_VALUE_START = "\uFF5F";
    public static final String PARAMETER_VALUE_END = "\uFF60";
	private Object stepsInstance;
	private Method method;
	private ParameterConverters parameterConverters;
	private StepMatcher stepMatcher;
    private StepMonitor stepMonitor;
    private Paranamer paranamer = new NullParanamer();
    private boolean dryRun = false;
	
    public StepCreator(Object stepsInstance, Method method,
			ParameterConverters parameterConverters, StepMatcher stepMatcher,
			StepMonitor stepMonitor) {
		this.stepsInstance = stepsInstance;
		this.method = method;
		this.parameterConverters = parameterConverters;
		this.stepMatcher = stepMatcher;
		this.stepMonitor = stepMonitor;
	}

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
    }

	public void useParanamer(Paranamer paranamer) {
		this.paranamer = paranamer;
	}

	public void doDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public Step createStep(final String stepAsString, Map<String, String> tableRow) {
        String[] annotationNames = annotatedParameterNames();
        String[] parameterNames = paranamer.lookupParameterNames(method, false);
        final Type[] types = method.getGenericParameterTypes();
        final String[] args = argsForStep(tableRow, types, annotationNames, parameterNames);
        final String parametrisedStep = parametrisedStep(stepAsString, tableRow, types, annotationNames, parameterNames, args);
        return new Step() {
            public StepResult perform() {
                try {
					stepMonitor.performing(stepAsString, dryRun);
					if (!dryRun) {
						method.invoke(stepsInstance, convertArgs(args, types));
					}
                    return StepResult.successful(stepAsString).withParameterValues(parametrisedStep);
                } catch (Throwable t) {
                    return failedOrPending(stepAsString, t);
                }
            }

            private StepResult failedOrPending(final String stepAsString, Throwable t) {
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    Throwable cause = t.getCause();
					if (cause instanceof PendingError) {
                        return StepResult.pending(stepAsString, (PendingError) cause).withParameterValues(parametrisedStep);
                    } else {
                        return StepResult.failed(stepAsString, cause).withParameterValues(parametrisedStep);
                    }
                }
                return StepResult.failed(stepAsString, t).withParameterValues(parametrisedStep);
            }

            public StepResult doNotPerform() {
                return StepResult.notPerformed(stepAsString).withParameterValues(parametrisedStep);
            }

        };
    }
    
    /**
     * Extract annotated parameter names from the @Named parameter annotations
     * 
     * @return An array of annotated parameter names, which <b>may</b> include
     *         <code>null</code> values for parameters that are not annotated
     */
    private String[] annotatedParameterNames() {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] names = new String[parameterAnnotations.length];
        for (int x = 0; x < parameterAnnotations.length; x++) {
            Annotation[] annotations = parameterAnnotations[x];
            for (Annotation annotation : annotations) {
                names[x] = annotationName(annotation);
            }
        }
        return names;
    }

    private String annotationName(Annotation annotation) {
        if (annotation.annotationType().isAssignableFrom(Named.class)) {
            return ((Named) annotation).value();
        } else if ("javax.inject.Named".equals(annotation.annotationType().getName())) {
            return Jsr330Helper.getNamedValue(annotation);
        } else {
            return null;
        }
    }
    
    private String parametrisedStep(String stepAsString, Map<String, String> tableRow, Type[] types, String[] annotationNames,
            String[] parameterNames, String[] args) {
        String parametrisedStep = stepAsString;
        for (int position = 0; position < types.length; position++) {
            parametrisedStep = replaceParameterValuesInStep(parametrisedStep, position, annotationNames, parameterNames, args, tableRow);
        }
        return parametrisedStep;
    }

    private String replaceParameterValuesInStep(String stepText, int position, String[] annotationNames, String[] parameterNames,
            String[] args, Map<String, String> tableRow) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        if (annotatedNamePosition != -1) {
            stepText = replaceTableValue(stepText, tableRow, annotationNames[position]);
        } else if (parameterNamePosition != -1) {
            stepText = replaceTableValue(stepText, tableRow, parameterNames[position]);
        }
        stepText = replaceArgValue(stepText, position, args);        	
        return stepText;
    }

	private String replaceArgValue(String stepText, int position, String[] args) {
		String value = args[position];
		if (value != null) {
		    stepText = stepText.replace(value, PARAMETER_VALUE_START + value + PARAMETER_VALUE_END);
		}
		return stepText;
	}

	private String replaceTableValue(String stepText, Map<String, String> tableRow,
			String name) {
		String value = getTableValue(tableRow, name);
		if (value != null) {
		    stepText = stepText.replace(PARAMETER_NAME_START + name + PARAMETER_NAME_END, PARAMETER_VALUE_START + value + PARAMETER_VALUE_END);
		}
		return stepText;
	}
    
    private String[] argsForStep(Map<String, String> tableRow, Type[] types, String[] annotationNames,
            String[] parameterNames) {
        final String[] args = new String[types.length];
        for (int position = 0; position < types.length; position++) {
            args[position] = argForPosition(position, annotationNames, parameterNames, tableRow);
        }
        return args;
    }

    private Object[] convertArgs(String[] argsAsString, Type[] types) {
        final Object[] args = new Object[argsAsString.length];
        for (int position = 0; position < argsAsString.length; position++) {
            args[position] = parameterConverters.convert(argsAsString[position], types[position]);
        }
        return args;
    }

    private String argForPosition(int position, String[] annotationNames, String[] parameterNames,
            Map<String, String> tableRow) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        String arg = null;
        if (annotatedNamePosition != -1 && isGroupName(annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingAnnotatedNameForArg(name, position);
            arg = matchedParameter(name);
        } else if (parameterNamePosition != -1 && isGroupName(parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingParameterNameForArg(name, position);
            arg = matchedParameter(name);
        } else if (annotatedNamePosition != -1 && isTableFieldName(tableRow, annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingTableAnnotatedNameForArg(name, position);
            arg = getTableValue(tableRow, name);
        } else if (parameterNamePosition != -1 && isTableFieldName(tableRow, parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingTableParameterNameForArg(name, position);
            arg = getTableValue(tableRow, name);
        } else {
            stepMonitor.usingNaturalOrderForArg(position);
            arg = matchedParameter(position);
        }
        stepMonitor.foundArg(arg, position);
        return arg;
    }
    
    private String matchedParameter(String name) {
        String[] parameterNames = stepMatcher.parameterNames();
		for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (name.equals(parameterName)) {
                return matchedParameter(i);
            }
        }
        throw new NoParameterFoundForName(name, parameterNames);
    }

	private String matchedParameter(int i) {
		return stepMatcher.parameter(i + 1);
	}

    private int parameterPosition(String[] names, int position) {
        if (names.length == 0) {
            return -1;
        }
        String positionName = names[position];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name != null && positionName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isGroupName(String name) {
        String[] groupNames = stepMatcher.parameterNames();
		for (String groupName : groupNames) {
            if (name.equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    private String getTableValue(Map<String, String> tableRow, String name) {
        return tableRow.get(name);
    }

    private boolean isTableFieldName(Map<String, String> tableRow, String name) {
        return tableRow.get(name) != null;
    }

    /**
     * This is a different class, because the @Inject jar may not be in the classpath.
     */
    public static class Jsr330Helper {

        private static String getNamedValue(Annotation annotation) {
            return ((javax.inject.Named) annotation).value();
        }

    }

    @SuppressWarnings("serial")
    public static class NoParameterFoundForName extends RuntimeException {

        public NoParameterFoundForName(String name, String[] names) {
            super("No parameter found for name '" + name + "' amongst '" + asList(names) + "'");
        }

    }


}
