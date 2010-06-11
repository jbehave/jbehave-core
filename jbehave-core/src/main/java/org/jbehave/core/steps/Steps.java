package org.jbehave.core.steps;

import static org.jbehave.core.annotations.AfterScenario.Outcome.ANY;
import static org.jbehave.core.annotations.AfterScenario.Outcome.FAILURE;
import static org.jbehave.core.annotations.AfterScenario.Outcome.SUCCESS;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporter;

/**
 * <p>
 * Default implementation of {@link CandidateSteps} which provides access to the
 * model of candidate steps that match the core you want to run.
 * </p>
 * <p>
 * To provide your candidate steps method, you can either extend the
 * {@link Steps} class or pass it a POJO instance that it can wrap.
 * In the former case, the instance is the {@link Steps} class itself.
 * </p>
 * <p>
 * You can define the methods that should be run by annotating them with @Given, @When
 * or @Then, and providing a value for each annotation that matches the step.
 * The value is interpreted by the {@link StepPatternParser}, which by default
 * interprets the '$' as parameters.
 * </p>
 * <p>
 * For instance, you could define a method as:
 * 
 * <pre>
 * &lt;code lang=&quot;java&quot;&gt; 
 * &#064;When(&quot;I log in as $username with password: $password&quot;) &lt;br/&gt; 
 * public void logIn(String username, String password) { //... } 
 * &lt;/code&gt;
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
 * When the step is perfomed, the parameters in the core model will be
 * passed to the class, so in this case the effect will be
 * 
 * <pre>
 * mySteps.logIn(&quot;Liz&quot;, &quot;Pa55word&quot;);
 * </pre>
 * 
 * </p>
 * <p>
 * StoryConfiguration can be used to provide customization to the defaults
 * configuration elements, eg custom parameters converters.
 * </p>
 */
public class Steps implements CandidateSteps {

    private final StoryConfiguration configuration;
    private Object instance;

    /**
     * Creates Steps with default configuration
     */
    public Steps() {
        this(new MostUsefulStoryConfiguration());
    }

    /**
     * Creates Steps with given custom configuration
     * 
     * @param configuration the StoryConfiguration
     */
    public Steps(StoryConfiguration configuration) {
        this.configuration = configuration;
        this.instance = this;
    }

    /**
     * Creates Steps with given custom configuration wrapping a POJO instance
     * containing the annotated steps methods
     * 
     * @param configuration the StoryConfiguration
     * @param instance the POJO instance
     */
    public Steps(StoryConfiguration configuration, Object instance) {
        this.configuration = configuration;
        this.instance = instance;
    }

    public CandidateStep[] getSteps() {
        return getSteps(instance.getClass());
    }

    public CandidateStep[] getSteps(Class<?> stepsClass) {
        List<CandidateStep> steps = new ArrayList<CandidateStep>();
        for (Method method : stepsClass.getMethods()) {
            if (method.isAnnotationPresent(Given.class)) {
                Given annotation = method.getAnnotation(Given.class);
                String value = encode(annotation.value());
                int priority = annotation.priority();
                createCandidateStep(steps, method, GIVEN, value, priority);
                createCandidateStepsFromAliases(steps, method, GIVEN, priority);
            }
            if (method.isAnnotationPresent(When.class)) {
                When annotation = method.getAnnotation(When.class);
                String value = encode(annotation.value());
                int priority = annotation.priority();
                createCandidateStep(steps, method, WHEN, value, priority);
                createCandidateStepsFromAliases(steps, method, WHEN, priority);
            }
            if (method.isAnnotationPresent(Then.class)) {
                Then annotation = method.getAnnotation(Then.class);
                String value = encode(annotation.value());
                int priority = annotation.priority();
                createCandidateStep(steps, method, THEN, value, priority);
                createCandidateStepsFromAliases(steps, method, THEN, priority);
            }
        }
        return steps.toArray(new CandidateStep[steps.size()]);
    }

    private String encode(String value) {
        return configuration.keywords().encode(value);
    }

    private void createCandidateStep(List<CandidateStep> steps, Method method, StepType stepType,
            String stepPatternAsString, int priority) {
        checkForDuplicateCandidateSteps(steps, stepType, stepPatternAsString);
        CandidateStep step = createCandidateStep(method, stepType, stepPatternAsString, priority, configuration);
        step.useStepMonitor(configuration.stepMonitor());
        step.useParanamer(configuration.paranamer());
        step.doDryRun(configuration.dryRun());
        steps.add(step);
    }

    protected CandidateStep createCandidateStep(Method method, StepType stepType, String stepPatternAsString, int priority,
            StoryConfiguration configuration) {
        return new CandidateStep(stepPatternAsString, priority, stepType, method, instance,
                configuration.stepPatternParser(), configuration.parameterConverters(), configuration.keywords().startingWordsByType());
    }

    private void checkForDuplicateCandidateSteps(List<CandidateStep> steps, StepType stepType, String patternAsString) {
        for (CandidateStep step : steps) {
            if (step.getStepType() == stepType && step.getPatternAsString().equals(patternAsString)) {
                throw new DuplicateCandidateStepFoundException(stepType, patternAsString);
            }
        }
    }

    private void createCandidateStepsFromAliases(List<CandidateStep> steps, Method method, StepType stepType, int priority) {
        if (method.isAnnotationPresent(Aliases.class)) {
            String[] aliases = method.getAnnotation(Aliases.class).values();
            for (String alias : aliases) {
                createCandidateStep(steps, method, stepType, alias, priority);
            }
        }
        if (method.isAnnotationPresent(Alias.class)) {
            String alias = method.getAnnotation(Alias.class).value();
            createCandidateStep(steps, method, stepType, alias, priority);
        }
    }

    public List<Step> runBeforeStory(boolean givenStory) {
        return storyStepsHaving(BeforeStory.class, givenStory, new DoRun());
    }

    public List<Step> runAfterStory(boolean givenStory) {
        return storyStepsHaving(AfterStory.class, givenStory, new DoRun());
    }
    
	public StoryConfiguration getConfiguration() {
		return configuration;
	}

    List<Step> storyStepsHaving(final Class<? extends Annotation> annotationClass, boolean givenStory, final StepRunner forSuccess) {
        List<Step> steps = new ArrayList<Step>();
        for (final Method method : methodsOf(instance)) {
            if (method.isAnnotationPresent(annotationClass)) {
                if ( runnableStoryStep(method.getAnnotation(annotationClass), givenStory) ){
                    steps.add(new Step() {
                        public StepResult doNotPerform() {
                            return forSuccess.run(annotationClass, method);
                        }

                        public StepResult perform() {
                            return forSuccess.run(annotationClass, method);
                        }
                    });                    
                }
            }
        }
        return steps;
    }

    private boolean runnableStoryStep(Annotation annotation, boolean givenStory) {
        boolean uponEmbedded = uponEmbedded(annotation);
        return uponEmbedded == givenStory;
    }    

    private boolean uponEmbedded(Annotation annotation) {
        if ( annotation instanceof BeforeStory ){
            return ((BeforeStory)annotation).uponEmbedded();
        } else  if ( annotation instanceof AfterStory ){
            return ((AfterStory)annotation).uponEmbedded();
        } 
        return false;
    }

    public List<Step> runBeforeScenario() {
        return scenarioStepsHaving(BeforeScenario.class, new DoRun());
    }

    public List<Step> runAfterScenario() {
        List<Step> steps = new ArrayList<Step>();
        steps.addAll(scenarioStepsHavingOutcome(AfterScenario.class, ANY, new DoRun(), new DoRun()));
        steps.addAll(scenarioStepsHavingOutcome(AfterScenario.class, SUCCESS, new DoRun(), new DoNotRun()));
        steps.addAll(scenarioStepsHavingOutcome(AfterScenario.class, FAILURE, new DoNotRun(), new DoRun()));
        return steps;
    }

    List<Step> scenarioStepsHaving(final Class<? extends Annotation> annotationClass, final StepRunner forSuccess) {
        List<Step> steps = new ArrayList<Step>();
        for (final Method method : methodsOf(instance)) {
            if (method.isAnnotationPresent(annotationClass)) {
                steps.add(new Step() {
                    public StepResult doNotPerform() {
                        return forSuccess.run(annotationClass, method);
                    }

                    public StepResult perform() {
                        return forSuccess.run(annotationClass, method);
                    }

                });
            }
        }
        return steps;
    }

    private List<Step> scenarioStepsHavingOutcome(final Class<? extends AfterScenario> annotationClass, final Outcome outcome,
            final StepRunner forSuccess, final StepRunner forFailure) {
        List<Step> steps = new ArrayList<Step>();
        for (final Method method : methodsOf(instance)) {
            if (method.isAnnotationPresent(annotationClass)) {
                AfterScenario annotation = method.getAnnotation(annotationClass);
                if (outcome.equals(annotation.uponOutcome())) {
                    steps.add(new Step() {

                        public StepResult doNotPerform() {
                            return forFailure.run(annotationClass, method);
                        }

                        public StepResult perform() {
                            return forSuccess.run(annotationClass, method);
                        }

                    });
                }
            }
        }
        return steps;
    }

    private Method[] methodsOf(Object instance) {
        Method[] methods;
        if (instance == null) {
            methods = this.getClass().getMethods();
        } else {
            methods = instance.getClass().getMethods();
        }
        return methods;
    }

    class DoRun implements StepRunner {
        public StepResult run(final Class<? extends Annotation> annotation, Method method) {
            try {
                method.invoke(instance);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    throw new BeforeOrAfterFailed(annotation, method, e.getCause());
                } else {
                    throw new RuntimeException(e);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return new SilentStepResult();
        }
    }

    private class DoNotRun implements StepRunner {
        public StepResult run(Class<? extends Annotation> annotation, Method method) {
            return new SilentStepResult();
        }
    }

    private interface StepRunner {
        StepResult run(Class<? extends Annotation> annotation, Method method);
    }

    public class SilentStepResult extends StepResult {
        public SilentStepResult() {
            super("");
        }

        @Override
        public void describeTo(StoryReporter reporter) {
        }
    }

    @SuppressWarnings("serial")
    public static class DuplicateCandidateStepFoundException extends RuntimeException {

        public DuplicateCandidateStepFoundException(StepType stepType, String patternAsString) {
            super(stepType + " " + patternAsString);
        }

    }
    
    @Override
    public String toString() {
       return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
}
