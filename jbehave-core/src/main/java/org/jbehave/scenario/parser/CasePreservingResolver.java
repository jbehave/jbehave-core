package org.jbehave.scenario.parser;

import org.jbehave.scenario.RunnableScenario;


/**
 * <p>
 * Resolves scenario names while preserving the Java scenario class case eg:
 * "org.jbehave.scenario.ICanLogin.java" -> "org/jbehave/scenario/ICanLogin".
 * </p>
 * <p>
 * By default no extension is used, but this can be configured via the
 * constructor so that we can resolve name to eg
 * "org/jbehave/scenario/ICanLogin.scenario".
 * </p>
 */
public class CasePreservingResolver extends AbstractScenarioNameResolver {

    public CasePreservingResolver() {
        super();
    }

    public CasePreservingResolver(String extension) {
    	super(extension);
    }

	@Override
	protected String resolveFileName(Class<? extends RunnableScenario> scenarioClass) {
		return scenarioClass.getSimpleName();
	}

}
