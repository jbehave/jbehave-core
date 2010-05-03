package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 * Default implementation of StepdocGenerator, which collates stepdocs for
 * methods annotated with {@link Given}, {@link When} and {@link Then} and their
 * aliases.
 */
public class DefaultStepdocGenerator implements StepdocGenerator {

	private static final String EMPTY_VALUE = "";
	private static final String[] NO_ALIASES = new String[]{};

	public List<Stepdoc> generate(CandidateSteps... steps) {
		List<Stepdoc> stepdocs = new LinkedList<Stepdoc>();
		for (CandidateSteps candidateSteps : steps) {
			stepdocs.addAll(generate(candidateSteps));
		}
		Collections.sort(stepdocs);
		return stepdocs;
	}

	private List<Stepdoc> generate(CandidateSteps candidateSteps) {
		List<Stepdoc> stepdocs = new LinkedList<Stepdoc>();
		for (Method method : candidateSteps.getClass().getMethods()) {
			if (method.isAnnotationPresent(Given.class)) {
				stepdocs.add(new Stepdoc(Given.class, method.getAnnotation(
						Given.class).value(), aliases(method), method,
						candidateSteps));
			}
			if (method.isAnnotationPresent(When.class)) {
				stepdocs.add(new Stepdoc(When.class, method.getAnnotation(
						When.class).value(), aliases(method), method,
						candidateSteps));
			}
			if (method.isAnnotationPresent(Then.class)) {
				stepdocs.add(new Stepdoc(Then.class, method.getAnnotation(
						Then.class).value(), aliases(method), method,
						candidateSteps));
			}
			if (method.isAnnotationPresent(BeforeScenario.class)) {
				stepdocs.add(new Stepdoc(BeforeScenario.class, EMPTY_VALUE, NO_ALIASES, method,
						candidateSteps));
			}
			if (method.isAnnotationPresent(AfterScenario.class)) {
				stepdocs.add(new Stepdoc(AfterScenario.class, EMPTY_VALUE, NO_ALIASES, method,
						candidateSteps));
			}
		}
		return stepdocs;
	}

	private String[] aliases(Method method) {
		if (method.isAnnotationPresent(Aliases.class)) {
			return method.getAnnotation(Aliases.class).values();
		}
		return NO_ALIASES;
	}

}
