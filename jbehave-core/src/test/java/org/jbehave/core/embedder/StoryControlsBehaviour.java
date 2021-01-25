package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StoryControlsBehaviour {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String NEW_VALUE = "newValue";
    private static final String EMPTY = "";

    @Parameters
    public static Collection<Object[]> data() {
        //@formatter:off
        return Arrays.asList(new Object[][] {
            {(Function<StoryControls, Object>) StoryControls::dryRun,                                      (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doDryRun((boolean) v),                                      asList(false, true)},
            {(Function<StoryControls, Object>) StoryControls::resetStateBeforeScenario,                    (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doResetStateBeforeScenario((boolean) v),                    asList(true, false)},
            {(Function<StoryControls, Object>) StoryControls::resetStateBeforeStory,                       (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doResetStateBeforeStory((boolean) v),                       asList(true, false)},
            {(Function<StoryControls, Object>) StoryControls::skipScenariosAfterFailure,                   (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doSkipScenariosAfterFailure((boolean) v),                   asList(false, true)},
            {(Function<StoryControls, Object>) StoryControls::skipBeforeAndAfterScenarioStepsIfGivenStory, (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doSkipBeforeAndAfterScenarioStepsIfGivenStory((boolean) v), asList(false, true)},
            {(Function<StoryControls, Object>) StoryControls::ignoreMetaFiltersIfGivenStory,               (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doIgnoreMetaFiltersIfGivenStory((boolean) v),               asList(false, true)},
            {(Function<StoryControls, Object>) StoryControls::metaByRow,                                   (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doMetaByRow((boolean) v),                                   asList(false, true)},
            {(Function<StoryControls, Object>) StoryControls::skipStoryIfGivenStoryFailed,                 (BiFunction<StoryControls, Object, Object>) (c, v) -> c.doSkipStoryIfGivenStoryFailed((boolean) v),                 asList(false, true)},
            {(Function<StoryControls, Object>) StoryControls::storyMetaPrefix,                             (BiFunction<StoryControls, Object, Object>) (c, v) -> c.useStoryMetaPrefix((String) v),                             asList(EMPTY, NEW_VALUE)},
            {(Function<StoryControls, Object>) StoryControls::scenarioMetaPrefix,                          (BiFunction<StoryControls, Object, Object>) (c, v) -> c.useScenarioMetaPrefix((String) v),                          asList(EMPTY, NEW_VALUE)}
        });
        //@formatter:on
    }

    @Parameter
    public Function<StoryControls, Object> getter;

    @Parameter(1)
    public BiFunction<StoryControls, Object, Object> setter;

    @Parameter(2)
    public List<Object> values;

    @Test
    public void shouldPrioritizeCurrentStoryControls() throws Throwable {
        Object defaultValue = values.get(0);
        Object threadLocalValue = values.get(1);
        StoryControls storyControls = new StoryControls();
        assertThat(getter.apply(storyControls), is(defaultValue));

        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertThat(getter.apply(storyControls), is(defaultValue));
                setter.apply(storyControls.currentStoryControls(), threadLocalValue);
                assertThat(getter.apply(storyControls), is(threadLocalValue));
                return null;
            }
        };

        executeInSeparateThread(task);
        assertThat(getter.apply(storyControls), is(defaultValue));
    }

    @Test
    public void shouldResetToCommonStoryControls() throws Throwable {
        Object defaultValue = values.get(0);
        Object threadLocalValue = values.get(1);
        StoryControls storyControls = new StoryControls();

        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setter.apply(storyControls.currentStoryControls(), threadLocalValue);
                assertThat(getter.apply(storyControls), is(threadLocalValue));
                storyControls.resetCurrentStoryControls();
                assertThat(getter.apply(storyControls), is(defaultValue));
                return null;
            }
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
    
    @AfterClass
    public static void closeExecutor() {
        EXECUTOR.shutdown();
    }
}
