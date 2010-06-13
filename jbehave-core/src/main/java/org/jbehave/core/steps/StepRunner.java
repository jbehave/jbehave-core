package org.jbehave.core.steps;

import java.lang.reflect.Method;

public interface StepRunner {
    
	StepResult run(Method method);

}