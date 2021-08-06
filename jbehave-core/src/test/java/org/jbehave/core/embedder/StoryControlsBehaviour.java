package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StoryControlsBehaviour {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String NEW_VALUE = "newValue";
    private static final String EMPTY = "";

    public static Stream<Arguments> data() {
        //@formatter:off
        return Stream.of(
                arguments((Function<StoryControls, Object>) StoryControls::dryRun,                                      (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doDryRun((boolean) v),                                      asList(false, true)),
                arguments((Function<StoryControls, Object>) StoryControls::resetStateBeforeScenario,                    (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doResetStateBeforeScenario((boolean) v),                    asList(true, false)),
                arguments((Function<StoryControls, Object>) StoryControls::resetStateBeforeStory,                       (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doResetStateBeforeStory((boolean) v),                       asList(true, false)),
                arguments((Function<StoryControls, Object>) StoryControls::skipScenariosAfterFailure,                   (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doSkipScenariosAfterFailure((boolean) v),                   asList(false, true)),
                arguments((Function<StoryControls, Object>) StoryControls::skipBeforeAndAfterScenarioStepsIfGivenStory, (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doSkipBeforeAndAfterScenarioStepsIfGivenStory((boolean) v), asList(false, true)),
                arguments((Function<StoryControls, Object>) StoryControls::ignoreMetaFiltersIfGivenStory,               (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doIgnoreMetaFiltersIfGivenStory((boolean) v),               asList(false, true)),
                arguments((Function<StoryControls, Object>) StoryControls::metaByRow,                                   (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doMetaByRow((boolean) v),                                   asList(false, true)),
                arguments((Function<StoryControls, Object>) StoryControls::skipStoryIfGivenStoryFailed,                 (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doSkipStoryIfGivenStoryFailed((boolean) v),                 asList(false, true)),
                arguments((Function<StoryControls, Object>) StoryControls::storyMetaPrefix,                             (BiFunction<StoryControls, Object, Object>) (c, v) -> c.useStoryMetaPrefix((String) v),                             asList(EMPTY, NEW_VALUE)),
                arguments((Function<StoryControls, Object>) StoryControls::scenarioMetaPrefix,                          (BiFunction<StoryControls, Object, Object>) (c, v) -> c.useScenarioMetaPrefix((String) v),                          asList(EMPTY, NEW_VALUE))
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldPrioritizeCurrentStoryControls(Function<StoryControls, Object> getter,
            BiFunction<StoryControls, Object, Object> setter, List<Object> values) throws Throwable {
        Object defaultValue = values.get(0);
        Object threadLocalValue = values.get(1);
        StoryControls storyControls = new StoryControls();
        assertThat(getter.apply(storyControls), is(defaultValue));

        Callable<Void> task = () -> {
            assertThat(getter.apply(storyControls), is(defaultValue));
            setter.apply(storyControls.currentStoryControls(), threadLocalValue);
            assertThat(getter.apply(storyControls), is(threadLocalValue));
            return null;
        };

        executeInSeparateThread(task);
        assertThat(getter.apply(storyControls), is(defaultValue));
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldResetToCommonStoryControls(Function<StoryControls, Object> getter,
            BiFunction<StoryControls, Object, Object> setter, List<Object> values) throws Throwable {
        Object defaultValue = values.get(0);
        Object threadLocalValue = values.get(1);
        StoryControls storyControls = new StoryControls();

        Callable<Void> task = () -> {
            setter.apply(storyControls.currentStoryControls(), threadLocalValue);
            assertThat(getter.apply(storyControls), is(threadLocalValue));
            storyControls.resetCurrentStoryControls();
            assertThat(getter.apply(storyControls), is(defaultValue));
            return null;
        };

        executeInSeparateThread(task);
        assertThat(getter.apply(storyControls), is(defaultValue));
    }

    private void executeInSeparateThread(Callable<Void> task) throws Throwable {
        try {
            EXECUTOR.submit(task).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
    
    @AfterAll
    public static void closeExecutor() {
        EXECUTOR.shutdown();
    }
}
