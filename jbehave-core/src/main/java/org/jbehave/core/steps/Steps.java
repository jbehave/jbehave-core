package org.jbehave.core.steps;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jbehave.core.annotations.AfterScenario.Outcome.ANY;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

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
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.AliasVariant;
import org.jbehave.core.parsers.AliasParser;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;

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
 * <code>@When("I log in as $username with password: $password")
 * public void logIn(String username, String password) { //... }
 * </code>
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

    private final Class<?> type;
    private final InjectableStepsFactory stepsFactory;

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
    }

    public Class<?> type() {
        return type;
    }

    public Object instance() {
        return stepsFactory.createInstanceOfType(type);
    }

    @Override
    public List<StepCandidate> listCandidates() {

        Map<StepType, List<org.jbehave.core.model.Alias>> aliases = findAliases();

        List<StepCandidate> candidates = new ArrayList<>();
        for (Method method : allMethods()) {
            if (method.isAnnotationPresent(Given.class)) {
                Given annotation = method.getAnnotation(Given.class);
                String value = annotation.value();
                int priority = annotation.priority();
                addCandidatesFromVariants(candidates, method, GIVEN, value, priority);
                addCandidatesFromAliases(candidates, method, GIVEN, priority);
                findVariants(GIVEN, value, aliases).forEach(variant -> addCandidatesFromVariants(candidates, method,
                        GIVEN, variant.getValue(), priority));
            }
            if (method.isAnnotationPresent(When.class)) {
                When annotation = method.getAnnotation(When.class);
                String value = annotation.value();
                int priority = annotation.priority();
                addCandidatesFromVariants(candidates, method, WHEN, value, priority);
                addCandidatesFromAliases(candidates, method, WHEN, priority);
                findVariants(WHEN, value, aliases).forEach(variant -> addCandidatesFromVariants(candidates, method,
                        WHEN, variant.getValue(), priority));
            }
            if (method.isAnnotationPresent(Then.class)) {
                Then annotation = method.getAnnotation(Then.class);
                String value = annotation.value();
                int priority = annotation.priority();
                addCandidatesFromVariants(candidates, method, THEN, value, priority);
                addCandidatesFromAliases(candidates, method, THEN, priority);
                findVariants(THEN, value, aliases).forEach(variant -> addCandidatesFromVariants(candidates, method,
                        THEN, variant.getValue(), priority));
            }
        }

        return candidates;
    }

    private static Collection<AliasVariant> findVariants(StepType stepType, String stepValue,
            Map<StepType, List<org.jbehave.core.model.Alias>> aliases) {
        return aliases.getOrDefault(stepType, Collections.emptyList())
                      .stream()
                      .filter(alias -> stepValue.equals(alias.getStepIdentifier()))
                      .map(org.jbehave.core.model.Alias::getVariants)
                      .flatMap(List::stream)
                      .collect(toList());
    }

    private Map<StepType, List<org.jbehave.core.model.Alias>> findAliases() {
        ResourceLoader resourceLoader = configuration().storyLoader();
        AliasParser aliasParser = configuration().aliasParser();

        Collection<org.jbehave.core.model.Alias> aliases = configuration().aliasPaths().stream()
                                           .map(resourceLoader::loadResourceAsText)
                                           .collect(collectingAndThen(toSet(), aliasParser::parse));

        return aliases.stream().collect(groupingBy(org.jbehave.core.model.Alias::getType, toList()));
    }

    private void addCandidatesFromVariants(List<StepCandidate> candidates, Method method, StepType stepType,
            String value, int priority) {
        String[] composedSteps = method.isAnnotationPresent(Composite.class)
                ? method.getAnnotation(Composite.class).steps() : new String[0];
        addCandidatesFromVariants(candidates, method, stepType, value, priority, type, stepsFactory, composedSteps);
    }

    private void addCandidatesFromAliases(List<StepCandidate> candidates, Method method, StepType stepType,
            int priority) {
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

    @Override
    public List<BeforeOrAfterStep> listBeforeStories() {
        return listSteps(BeforeStories.class, a -> true, BeforeStories::order);
    }

    @Override
    public List<BeforeOrAfterStep> listAfterStories() {
        return listSteps(AfterStories.class, a -> true, AfterStories::order);
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeStory(boolean givenStory) {
        return listSteps(BeforeStory.class, a -> a.uponGivenStory() == givenStory, BeforeStory::order);
    }

    @Override
    public List<BeforeOrAfterStep> listAfterStory(boolean givenStory) {
        return listSteps(AfterStory.class, a -> a.uponGivenStory() == givenStory, AfterStory::order);
    }

    @Override
    public Map<ScenarioType, List<BeforeOrAfterStep>> listBeforeScenario() {
        return listBeforeOrAfterScenarioSteps(BeforeScenario.class, (a, scenarioType) -> a.uponType() == scenarioType,
                BeforeScenario::order, a -> ANY);
    }

    @Override
    public Map<ScenarioType, List<BeforeOrAfterStep>> listAfterScenario() {
        return listBeforeOrAfterScenarioSteps(AfterScenario.class, (a, scenarioType) -> a.uponType() == scenarioType,
                AfterScenario::order, AfterScenario::uponOutcome);
    }

    public <T extends Annotation> Map<ScenarioType, List<BeforeOrAfterStep>> listBeforeOrAfterScenarioSteps(
            Class<T> annotationClass, BiPredicate<T, ScenarioType> predicate, ToIntFunction<T> order,
            Function<T, Outcome> outcome) {
        StepCreator stepCreator = createStepCreator(type, stepsFactory);
        Map<Method, T> methods = methodsAnnotatedWith(annotationClass);
        Map<ScenarioType, List<BeforeOrAfterStep>> stepsPerType = new EnumMap<>(ScenarioType.class);
        for (ScenarioType scenarioType : ScenarioType.values()) {
            stepsPerType.put(scenarioType, listSteps(methods, a -> predicate.test(a, scenarioType), order, outcome,
                    stepCreator));
        }
        return stepsPerType;
    }

    private <T extends Annotation> List<BeforeOrAfterStep> listSteps(Class<T> annotationClass, Predicate<T> predicate,
            ToIntFunction<T> order) {
        StepCreator stepCreator = createStepCreator(type, stepsFactory);
        return listSteps(methodsAnnotatedWith(annotationClass), predicate, order, a -> ANY, stepCreator);
    }

    private <T extends Annotation> List<BeforeOrAfterStep> listSteps(Map<Method, T> methods, Predicate<T> predicate,
            ToIntFunction<T> order, Function<T, Outcome> outcome, StepCreator stepCreator) {
        return methods.entrySet()
                .stream()
                .filter(e -> predicate.test(e.getValue()))
                .map(e -> {
                    Method method = e.getKey();
                    T annotation = e.getValue();
                    return new BeforeOrAfterStep(method, order.applyAsInt(annotation), outcome.apply(annotation),
                            stepCreator);
                })
                .collect(toList());
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
