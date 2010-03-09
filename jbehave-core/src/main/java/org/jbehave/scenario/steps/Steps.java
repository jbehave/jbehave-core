package org.jbehave.scenario.steps;

import static org.jbehave.scenario.annotations.AfterScenario.Outcome.ANY;
import static org.jbehave.scenario.annotations.AfterScenario.Outcome.FAILURE;
import static org.jbehave.scenario.annotations.AfterScenario.Outcome.SUCCESS;
import static org.jbehave.scenario.steps.StepType.GIVEN;
import static org.jbehave.scenario.steps.StepType.THEN;
import static org.jbehave.scenario.steps.StepType.WHEN;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.scenario.annotations.AfterScenario;
import org.jbehave.scenario.annotations.AfterStory;
import org.jbehave.scenario.annotations.Alias;
import org.jbehave.scenario.annotations.Aliases;
import org.jbehave.scenario.annotations.BeforeScenario;
import org.jbehave.scenario.annotations.BeforeStory;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.annotations.AfterScenario.Outcome;
import org.jbehave.scenario.definition.KeyWords;
import org.jbehave.scenario.errors.BeforeOrAfterException;
import org.jbehave.scenario.parser.StepPatternBuilder;
import org.jbehave.scenario.reporters.ScenarioReporter;

/**
 * <p>
 * Default implementation of {@link CandidateSteps} which provides access to the
 * definition of candidate steps that match the scenario you want to run.
 * </p>
 * <p>
 * To provide your candidate steps method, you can either extend the
 * {@link Steps} class or pass it a POJO instance that it can wrap.
 * In the former case, the instance is the {@link Steps} class itself.
 * </p>
 * <p>
 * You can define the methods that should be run by annotating them with @Given, @When
 * or @Then, and providing a value for each annotation that matches the step.
 * The value is interpreted by the {@link StepPatternBuilder}, which by default
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
 * When the step is perfomed, the parameters in the scenario definition will be
 * passed to the class, so in this case the effect will be
 * 
 * <pre>
 * mySteps.logIn(&quot;Liz&quot;, &quot;Pa55word&quot;);
 * </pre>
 * 
 * </p>
 * <p>
 * StepsConfiguration can be used to provide customization to the defaults
 * configuration elements, eg custom parameters converters.
 * </p>
 */
public class Steps implements CandidateSteps {

    private final StepsConfiguration configuration;
    private Object instance;

    /**
     * Creates Steps with default configuration
     */
    public Steps() {
        this(new StepsConfiguration());
    }

    /**
     * Creates Steps with all default configuration except for custom starting
     * keywords
     * 
     * @param keywords the KeyWords which hold the words with which we expect
     *            steps in the scenarios to start
     */
    public Steps(KeyWords keywords) {
        this(new StepsConfiguration(keywords));
    }

    /**
     * Creates Steps with all default configuration except for custom starting
     * keywords
     * 
     * @param startingWords the words with which we expect steps in the
     *            scenarios to start
     * @deprecated Use Steps(KeyWords)
     */
    public Steps(String... startingWords) {
        this(new StepsConfiguration(startingWords));
    }

    /**
     * Creates Steps with all default dependencies except for custom parameter
     * converters.
     * 
     * @param converters a set of converters which can change strings into other
     *            objects to pass into executable steps
     */
    public Steps(ParameterConverters converters) {
        this(new StepsConfiguration(converters));
    }

    /**
     * Creates Steps with given custom configuration
     * 
     * @param configuration the StepsConfiguration
     */
    public Steps(StepsConfiguration configuration) {
        this.configuration = configuration;
        this.instance = this;
    }

    /**
     * Creates Steps with given custom configuration wrapping a POJO instance
     * containing the annotatated steps methods
     * 
     * @param configuration the StepsConfiguration
     * @param instance the POJO instance
     */
    public Steps(StepsConfiguration configuration, Object instance) {
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
        return configuration.getKeywords().encode(value);
    }

    private void createCandidateStep(List<CandidateStep> steps, Method method, StepType stepType,
            String stepPatternAsString, int priority) {
        checkForDuplicateCandidateSteps(steps, stepType, stepPatternAsString);
        CandidateStep step = createCandidateStep(method, stepType, stepPatternAsString, priority, configuration);
        step.useStepMonitor(configuration.getMonitor());
        step.useParanamer(configuration.getParanamer());
        steps.add(step);
    }

    protected CandidateStep createCandidateStep(Method method, StepType stepType, String stepPatternAsString, int priority,
            StepsConfiguration configuration) {
        return new CandidateStep(stepPatternAsString, priority, stepType, method, instance,
                configuration.getPatternBuilder(), configuration.getParameterConverters(), configuration.getStartingWordsByType());
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

    public List<Step> runBeforeStory(boolean embeddedStory) {
        return storyStepsHaving(BeforeStory.class, embeddedStory, new OkayToRun());
    }

    public List<Step> runAfterStory(boolean embeddedStory) {
        return storyStepsHaving(AfterStory.class, embeddedStory, new OkayToRun());
    }

    List<Step> storyStepsHaving(final Class<? extends Annotation> annotationClass, boolean embeddedStory, final StepPart forSuccess) {
        List<Step> steps = new ArrayList<Step>();
        for (final Method method : methodsOf(instance)) {
            if (method.isAnnotationPresent(annotationClass)) {
                if ( runnableStoryStep(method.getAnnotation(annotationClass), embeddedStory) ){
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

    private boolean runnableStoryStep(Annotation annotation, boolean embeddedStory) {
        boolean uponEmbedded = uponEmbedded(annotation);
        return uponEmbedded == embeddedStory;
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
        return scenarioStepsHaving(BeforeScenario.class, new OkayToRun());
    }

    public List<Step> runAfterScenario() {
        List<Step> steps = new ArrayList<Step>();
        steps.addAll(scenarioStepsHavingOutcome(AfterScenario.class, ANY, new OkayToRun(), new OkayToRun()));
        steps.addAll(scenarioStepsHavingOutcome(AfterScenario.class, SUCCESS, new OkayToRun(), new DoNotRun()));
        steps.addAll(scenarioStepsHavingOutcome(AfterScenario.class, FAILURE, new DoNotRun(), new OkayToRun()));
        return steps;
    }

    List<Step> scenarioStepsHaving(final Class<? extends Annotation> annotationClass, final StepPart forSuccess) {
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
            final StepPart forSuccess, final StepPart forFailure) {
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

    class OkayToRun implements StepPart {
        public StepResult run(final Class<? extends Annotation> annotation, Method method) {
            try {
                method.invoke(instance);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    throw new BeforeOrAfterException(annotation, method, e.getCause());
                } else {
                    throw new RuntimeException(e);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return new SilentStepResult();
        }
    }

    private class DoNotRun implements StepPart {
        public StepResult run(Class<? extends Annotation> annotation, Method method) {
            return new SilentStepResult();
        }
    }

    private interface StepPart {
        StepResult run(Class<? extends Annotation> annotation, Method method);
    }

    public class SilentStepResult extends StepResult {
        public SilentStepResult() {
            super("");
        }

        @Override
        public void describeTo(ScenarioReporter reporter) {
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
