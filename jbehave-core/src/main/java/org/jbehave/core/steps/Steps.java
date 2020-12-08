package org.jbehave.core.steps;

import static org.jbehave.core.annotations.AfterScenario.Outcome.ANY;
import static org.jbehave.core.annotations.AfterScenario.Outcome.FAILURE;
import static org.jbehave.core.annotations.AfterScenario.Outcome.SUCCESS;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * <p>
 * Default implementation of {@link CandidateSteps} which provides the step
 * candidates that match the steps being run.
 * </p>
 * <p>
 * To provide your step candidate methods, you can:
 * <ul>
 * <li>pass in the steps instance type and the steps factory used to instantiate
 * the instance if any candidate steps are matched (lazy "has-a" relationship)</li>
 * <li>pass in the steps instance, instantiated regardless of whether the
 * candidate steps are matched (eager "has-a" relationship)</li>
 * <li>extend the {@link Steps} class, in which case the instance is the
 * extended {@link Steps} class itself ("is-a" relationship)</li>
 * </ul>
 * <b>The "has-a" design model, in which the steps instance is passed in, is
 * strongly recommended over the "is-a" model as it does not have tie-ins in the
 * {@link Steps} class implementation</b>.
 * </p>
 * <p>
 * You can define the methods that should be run by annotating them with
 * {@link Given @Given}, {@link When @When} or {@link Then @Then}, and providing
 * as a value for each annotation a pattern matches the textual step. The value
 * is interpreted by the {@link StepPatternParser}, which by default is a
 * {@link RegexPrefixCapturingPatternParser} that interprets the words starting
 * with '$' as parameters.
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
 * {@link StepCandidate}s that are created, e.g. providing a step monitor or
 * creating them in "dry run" mode.
 * </p>
 */
public class Steps extends AbstractCandidateSteps {

    private Class<?> type;
    private InjectableStepsFactory stepsFactory;
    private StepCreator stepCreator;

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
     * @param configuration the Configuration
     */
    public Steps(Configuration configuration) {
        super(configuration);
        this.type = this.getClass();
        this.stepsFactory = new InstanceStepsFactory(configuration, this);
        stepCreator = new StepCreator(type, stepsFactory, configuration().stepsContext(),
                configuration().parameterConverters(), configuration().parameterControls(), null,
                configuration().stepMonitor());
    }

    /**
     * Creates Steps with given custom configuration and a steps instance
     * containing the candidate step methods
     * 
     * @param configuration the Configuration
     * @param instance the steps instance
     */
    public Steps(Configuration configuration, Object instance) {
        this(configuration, instance.getClass(), new InstanceStepsFactory(configuration, instance));
    }

    /**
     * Creates Steps with given custom configuration and a steps instance type
     * containing the candidate step methods. The steps instance is created
     * using the steps instance factory provided.
     * 
     * @param configuration the Configuration
     * @param type the steps instance type
     * @param stepsFactory the {@link InjectableStepsFactory}
     */
    public Steps(Configuration configuration, Class<?> type, InjectableStepsFactory stepsFactory) {
        super(configuration);
        this.type = type;
        this.stepsFactory = stepsFactory;
        stepCreator = new StepCreator(type, stepsFactory, configuration().stepsContext(),
                configuration().parameterConverters(), configuration().parameterControls(), null,
                configuration().stepMonitor());
    }

    public Class<?> type() {
        return type;
    }

    public Object instance() {
        return stepsFactory.createInstanceOfType(type);
    }

    @Override
    public List<StepCandidate> listCandidates() {
        List<StepCandidate> candidates = new ArrayList<>();
        for (Method method : allMethods()) {
            if (method.isAnnotationPresent(Given.class)) {
                Given annotation = method.getAnnotation(Given.class);
                String value = annotation.value();
                int priority = annotation.priority();
                addCandidatesFromVariants(candidates, method, GIVEN, value, priority);
                addCandidatesFromAliases(candidates, method, GIVEN, priority);
            }
            if (method.isAnnotationPresent(When.class)) {
                When annotation = method.getAnnotation(When.class);
                String value = annotation.value();
                int priority = annotation.priority();
                addCandidatesFromVariants(candidates, method, WHEN, value, priority);
                addCandidatesFromAliases(candidates, method, WHEN, priority);
            }
            if (method.isAnnotationPresent(Then.class)) {
                Then annotation = method.getAnnotation(Then.class);
                String value = annotation.value();
                int priority = annotation.priority();
                addCandidatesFromVariants(candidates, method, THEN, value, priority);
                addCandidatesFromAliases(candidates, method, THEN, priority);
            }
        }
        return candidates;
    }

    private void addCandidatesFromVariants(List<StepCandidate> candidates, Method method, StepType stepType, String value, int priority) {
        PatternVariantBuilder b = new PatternVariantBuilder(value);
        for (String variant : b.allVariants()) {
            addCandidate(candidates, method, stepType, variant, priority);
        }
    }
    
    private void addCandidatesFromAliases(List<StepCandidate> candidates, Method method, StepType stepType, int priority) {
        if (method.isAnnotationPresent(Aliases.class)) {
            String[] aliases = method.getAnnotation(Aliases.class).values();
            for (String alias : aliases) {
                addCandidatesFromVariants(candidates, method, stepType, alias, priority);
            }
        }
        if (method.isAnnotationPresent(Alias.class)) {
            String alias = method.getAnnotation(Alias.class).value();
            addCandidatesFromVariants(candidates, method, stepType, alias, priority);
        }
    }

    private void addCandidate(List<StepCandidate> candidates, Method method, StepType stepType,
            String stepPatternAsString, int priority) {
        StepCandidate candidate = createCandidate(stepPatternAsString, priority, stepType, method, type, stepsFactory);
        checkForDuplicateCandidates(candidates, candidate);
        if (method.isAnnotationPresent(Composite.class)) {
            candidate.composedOf(method.getAnnotation(Composite.class).steps());
        }
        candidates.add(candidate);
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterStories() {
        List<BeforeOrAfterStep> steps = new ArrayList<>();
        steps.addAll(listSteps(BeforeStories.class, Stage.BEFORE, v -> true, BeforeStories::order));
        steps.addAll(listSteps(AfterStories.class, Stage.AFTER, v -> true, AfterStories::order));
        return steps;
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterStory(boolean givenStory) {
        List<BeforeOrAfterStep> steps = new ArrayList<>();
        steps.addAll(listSteps(BeforeStory.class, Stage.BEFORE, v -> v.uponGivenStory() == givenStory, BeforeStory::order));
        steps.addAll(listSteps(AfterStory.class, Stage.AFTER, v -> v.uponGivenStory() == givenStory, AfterStory::order));
        return steps;
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterScenario(ScenarioType type) {
        List<BeforeOrAfterStep> steps = new ArrayList<>();
        steps.addAll(listSteps(BeforeScenario.class, Stage.BEFORE, v -> v.uponType() == type, BeforeScenario::order));
        for (Outcome outcome : new Outcome[] { ANY, SUCCESS, FAILURE }) {
            steps.addAll(listSteps(AfterScenario.class, Stage.AFTER,
                m -> new BeforeOrAfterStep(Stage.AFTER, m, outcome, stepCreator),
                v -> v.uponType() == type && v.uponOutcome() == outcome, AfterScenario::order));
        }
        return steps;
    }

    private <T extends Annotation> List<BeforeOrAfterStep> listSteps(Class<T> type, Stage stage, Predicate<T> predicate,
            ToIntFunction<T> order) {
        return listSteps(type, stage, m -> new BeforeOrAfterStep(stage, m, stepCreator), predicate, order);
    }

    private <T extends Annotation> List<BeforeOrAfterStep> listSteps(Class<T> type, Stage stage,
            Function<Method, BeforeOrAfterStep> factory, Predicate<T> predicate, ToIntFunction<T> order) {
        Comparator<Integer> orderComparator = Stage.AFTER == stage ? Comparator.naturalOrder()
                : Comparator.reverseOrder();
        return methodsAnnotatedWith(type).entrySet().stream()
                                                    .filter(e -> predicate.test(e.getValue()))
                                                    .collect(Collectors.toList())
                                                    .stream()
                                                    .sorted(Comparator.comparing(e -> order.applyAsInt(e.getValue()), orderComparator))
                                                    .map(Map.Entry::getKey)
                                                    .map(factory)
                                                    .collect(Collectors.toList());
    }

    private Method[] allMethods() {
        return type.getMethods();
    }

    private <T extends Annotation> Map<Method, T> methodsAnnotatedWith(Class<T> annotationClass) {
        Map<Method, T> annotated = new LinkedHashMap<>();
        for (Method method : allMethods()) {
            T annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                annotated.put(method, annotation);
            }
        }
        return annotated;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(instance()).toString();
    }
}
