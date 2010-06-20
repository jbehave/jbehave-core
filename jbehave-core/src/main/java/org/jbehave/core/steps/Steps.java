package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.jbehave.core.annotations.AfterScenario.Outcome.ANY;
import static org.jbehave.core.annotations.AfterScenario.Outcome.FAILURE;
import static org.jbehave.core.annotations.AfterScenario.Outcome.SUCCESS;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * <p>
 * Implementation of {@link CandidateSteps} which provides access to the
 * candidate steps that match the story you want to run.
 * </p>
 * <p>
 * To provide your candidate steps methods, you can either extend the
 * {@link Steps} class or pass it any {@link Object} instance that it can wrap.
 * In the former case, the instance is the extended {@link Steps} class itself.
 * Both "has-a" relationship and "is-a" design models are thus supported.
 * </p>
 * <p>
 * You can define the methods that should be run by annotating them with @Given, @When
 * or @Then, and providing as a value for each annotation a pattern matches the
 * textual step. The value is interpreted by the {@link StepPatternParser},
 * which by default is a {@link RegexPrefixCapturingPatternParser} that
 * interprets the words starting with '$' as parameters.
 * </p>
 * <p>
 * For instance, you could define a method as:
 * 
 * <pre>
 * @When("I log in as $username with password: $password")
 * public void logIn(String username, String password) { //... }
 * </pre>
 * 
 * and this would match the step:
 * 
 * <pre>
 * When I log in as Liz with password: Pa55word
 * </pre>
 * 
 * </p>
 * <p>
 * When the step is performed, the parameters matched will be passed to the
 * method, so in this case the effect will be to invoke:
 * </p>
 * 
 * <pre>
 * logIn(&quot;Liz&quot;, &quot;Pa55word&quot;);
 * </pre>
 * <p>
 * The {@link Configuration} can be used to provide customize the
 * {@link CandidateStep} that are created, e.g. providing a step monitor or
 * creating them in "dry run" mode.
 * </p>
 */
public class Steps implements CandidateSteps {

	private final Configuration configuration;
	private final Object instance;

	/**
	 * Creates Steps with default configuration for a class extending this
	 * instance and containing the candidate step methods
	 */
	public Steps() {
		this(new MostUsefulConfiguration());
	}

	/**
	 * Creates Steps with given custom configuration for a class extending this
	 * instance and containing the candidate step methods
	 * 
	 * @param configuration
	 *            the Configuration
	 */
	public Steps(Configuration configuration) {
		this(configuration, null);
	}

	/**
	 * Creates Steps with given custom configuration wrapping an Object instance
	 * containing the candidate step methods
	 * 
	 * @param configuration
	 *            the Configuration
	 * @param instance
	 *            the Object instance
	 */
	public Steps(Configuration configuration, Object instance) {
		this.configuration = configuration;
		this.instance = instance;
	}

	Object instance() {
		if (instance == null) {
			return this;
		}
		return instance;
	}

	public Configuration configuration() {
		return configuration;
	}

	public List<CandidateStep> listCandidates() {
		List<CandidateStep> candidates = new ArrayList<CandidateStep>();
		for (Method method : allMethods()) {
			if (method.isAnnotationPresent(Given.class)) {
				Given annotation = method.getAnnotation(Given.class);
				String value = annotation.value();
				int priority = annotation.priority();
				addCandidateStep(candidates, method, GIVEN, value, priority);
				addCandidateStepsFromAliases(candidates, method, GIVEN,
						priority);
			}
			if (method.isAnnotationPresent(When.class)) {
				When annotation = method.getAnnotation(When.class);
				String value = annotation.value();
				int priority = annotation.priority();
				addCandidateStep(candidates, method, WHEN, value, priority);
				addCandidateStepsFromAliases(candidates, method, WHEN, priority);
			}
			if (method.isAnnotationPresent(Then.class)) {
				Then annotation = method.getAnnotation(Then.class);
				String value = annotation.value();
				int priority = annotation.priority();
				addCandidateStep(candidates, method, THEN, value, priority);
				addCandidateStepsFromAliases(candidates, method, THEN, priority);
			}
		}
		return candidates;
	}

	private void addCandidateStep(List<CandidateStep> candidates,
			Method method, StepType stepType, String stepPatternAsString,
			int priority) {
		checkForDuplicateCandidateSteps(candidates, stepType,
				stepPatternAsString);
		CandidateStep step = createCandidateStep(method, stepType,
				stepPatternAsString, priority, configuration);
		step.useStepMonitor(configuration.stepMonitor());
		step.useParanamer(configuration.paranamer());
		step.doDryRun(configuration.dryRun());
		candidates.add(step);
	}

	private void checkForDuplicateCandidateSteps(
			List<CandidateStep> candidates, StepType stepType,
			String patternAsString) {
		for (CandidateStep candidate : candidates) {
			if (candidate.getStepType() == stepType
					&& candidate.getPatternAsString().equals(patternAsString)) {
				throw new DuplicateCandidateStepFoundException(stepType,
						patternAsString);
			}
		}
	}

	private void addCandidateStepsFromAliases(List<CandidateStep> candidates,
			Method method, StepType stepType, int priority) {
		if (method.isAnnotationPresent(Aliases.class)) {
			String[] aliases = method.getAnnotation(Aliases.class).values();
			for (String alias : aliases) {
				addCandidateStep(candidates, method, stepType, alias, priority);
			}
		}
		if (method.isAnnotationPresent(Alias.class)) {
			String alias = method.getAnnotation(Alias.class).value();
			addCandidateStep(candidates, method, stepType, alias, priority);
		}
	}

	private CandidateStep createCandidateStep(Method method, StepType stepType,
			String stepPatternAsString, int priority,
			Configuration configuration) {
		return new CandidateStep(stepPatternAsString, priority, stepType,
				method, instance(), configuration.keywords()
						.startingWordsByType(), configuration
						.stepPatternParser(), configuration
						.parameterConverters());
	}
	
	public List<BeforeOrAfterStep> listBeforeOrAfterStory(boolean givenStory){
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		steps.addAll(stepsHaving(Stage.BEFORE, BeforeStory.class, givenStory));
		steps.addAll(stepsHaving(Stage.AFTER, AfterStory.class, givenStory));
		return steps;
	}

	public List<BeforeOrAfterStep> listBeforeOrAfterScenario(){
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		steps.addAll(stepsHaving(Stage.BEFORE, BeforeScenario.class));
		steps.addAll(stepsHaving(Stage.AFTER, AfterScenario.class, ANY));
		steps.addAll(stepsHaving(Stage.AFTER, AfterScenario.class, SUCCESS));
		steps.addAll(stepsHaving(Stage.AFTER, AfterScenario.class, FAILURE));
		return steps;
	}

	private List<BeforeOrAfterStep> stepsHaving(
			Stage stage, Class<? extends Annotation> annotationClass, boolean givenStory) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		for (final Method method : annotatatedMethods(annotationClass)) {
			if (runnableStoryStep(method.getAnnotation(annotationClass),
					givenStory)) {
				steps.add(createBeforeOrAfterStep(stage, method));
			}
		}
		return steps;
	}

	private boolean runnableStoryStep(Annotation annotation, boolean givenStory) {
		boolean uponGivenStory = uponGivenStory(annotation);
		return uponGivenStory == givenStory;
	}

	private boolean uponGivenStory(Annotation annotation) {
		if (annotation instanceof BeforeStory) {
			return ((BeforeStory) annotation).uponGivenStory();
		} else if (annotation instanceof AfterStory) {
			return ((AfterStory) annotation).uponGivenStory();
		}
		return false;
	}

	private List<BeforeOrAfterStep> stepsHaving(Stage stage,
			Class<? extends Annotation> annotationClass) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		for (Method method : annotatatedMethods(annotationClass)) {
			steps.add(createBeforeOrAfterStep(stage, method));
		}
		return steps;
	}

	private List<BeforeOrAfterStep> stepsHaving(Stage stage,
			Class<? extends AfterScenario> annotationClass, Outcome outcome) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		for (Method method : annotatatedMethods(annotationClass)) {
			AfterScenario annotation = method.getAnnotation(annotationClass);
			if (outcome.equals(annotation.uponOutcome())) {
				steps.add(createBeforeOrAfterStep(stage, method, outcome));
			}
		}
		return steps;
	}


	private BeforeOrAfterStep createBeforeOrAfterStep(Stage stage, Method method) {
		return new BeforeOrAfterStep(stage, method, instance());
	}

	private BeforeOrAfterStep createBeforeOrAfterStep(Stage stage, Method method, Outcome outcome) {
		return new BeforeOrAfterStep(stage, method, instance(), outcome);
	}

	private List<Method> allMethods() {
		return asList(instance().getClass().getMethods());
	}

	private List<Method> annotatatedMethods(
			Class<? extends Annotation> annotationClass) {
		List<Method> annotated = new ArrayList<Method>();
		for (Method method : allMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				annotated.add(method);
			}
		}
		return annotated;
	}

	@SuppressWarnings("serial")
	public static class DuplicateCandidateStepFoundException extends
			RuntimeException {

		public DuplicateCandidateStepFoundException(StepType stepType,
				String patternAsString) {
			super(stepType + " " + patternAsString);
		}

	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,
				ToStringStyle.SHORT_PREFIX_STYLE).append(instance()).toString();
	}

}
