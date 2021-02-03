package org.jbehave.core.reporters;

import static org.jbehave.core.reporters.PrintStreamOutput.Format.JSON;
import static org.jbehave.core.steps.StepCreator.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;

/**
 * <p>
 * Story reporter that outputs to a PrintStream, as JSON. It extends
 * {@link PrintStreamOutput}, providing JSON-based default output
 * patterns, which can be overridden via the {@link
 * JsonOutput (PrintStream,Properties)} constructor.
 * </p>
 *
 * @author Valery Yatsynovich
 */
public class JsonOutput extends PrintStreamOutput {

    private static final char JSON_DOCUMENT_START = 0;
    private static final char JSON_OBJECT_START = '{';
    private static final char JSON_ARRAY_START = '[';
    private static final char[] JSON_START_CHARS = { JSON_DOCUMENT_START, JSON_OBJECT_START, JSON_ARRAY_START };

    private static final String[] STEP_KEYS = { "successful", "ignorable", "comment", "pending", "notPerformed",
            "failed", "restarted" };

    private char lastChar = JSON_DOCUMENT_START;

    private int givenStoriesLevel = 0;
    private int storyPublishingLevel = 0;
    private boolean stepPublishing = false;

    public JsonOutput(PrintStream output, Keywords keywords) {
        this(output, new Properties(), keywords);
    }

    public JsonOutput(PrintStream output, Properties outputPatterns, Keywords keywords) {
        super(JSON, output, defaultXmlPatterns(), outputPatterns, keywords);
    }

    @Override
    protected void print(PrintStream output, String text) {
        if (!text.isEmpty()) {
            boolean doNotAddComma =
                    ArrayUtils.contains(JSON_START_CHARS, lastChar) || StringUtils.startsWithAny(text, "}", "]", ",");
            super.print(output, doNotAddComma ? text : "," + text);
            lastChar = text.charAt(text.length() - 1);
        }
    }

    @Override
    protected String format(String key, String defaultPattern, Object... args) {
        if ("beforeGivenStories".equals(key)) {
            givenStoriesLevel++;
        } else if ("afterGivenStories".equals(key)) {
            if (storyPublishingLevel == givenStoriesLevel) {
                // Closing given "stories"
                print("]");
                storyPublishingLevel--;
            }
            givenStoriesLevel--;
            return super.format(key, defaultPattern, args);
        } else if ("beforeStory".equals(key) && storyPublishingLevel < givenStoriesLevel) {
            // Starting given "stories"
            print("\"stories\": [");
            storyPublishingLevel ++;
        }
        //Closing "examples" if "steps" are empty
        if ("exampleScenariosEnd".equals(key) && !stepPublishing) {
            print("}");
        }
        if (stepPublishing) {
            if ("exampleScenariosEnd".equals(key) || "example".equals(key) && givenStoriesLevel == 0) {
                // Closing previous "example"
                print("}");
            }
            if ("afterScenario".equals(key) || "afterScenarioWithFailure".equals(key)) {
                stepPublishing = false;
            }
        } else if (ArrayUtils.contains(STEP_KEYS, key)) {
            stepPublishing = true;
        }
        return super.format(key, defaultPattern, args);
    }

    @Override
    protected String transformPrintingTable(String text, String tableStart, String tableEnd) {
        return text;
    }

    private static Properties defaultXmlPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("dryRun", "\"dryRun\": \"{0}\"");
        patterns.setProperty("beforeStory", "'{'\"path\": \"{1}\", \"title\": \"{0}\"");
        patterns.setProperty("storyCancelled", "'{'\"cancelled\": '{'\"keyword\": \"{0}\", \"durationKeyword\": \"{1}\", \"durationInSecs\": \"{2}\"}}");
        patterns.setProperty("afterStory", "}");
        patterns.setProperty("beforeScenarios", "\"scenarios\": [");
        patterns.setProperty("afterScenarios", "]");
        patterns.setProperty("afterStory", "}");
        patterns.setProperty("pendingMethodsStart", "\"pendingMethods\": [");
        patterns.setProperty("pendingMethod", "\"{0}\"");
        patterns.setProperty("pendingMethodsEnd", "]");
        patterns.setProperty("metaStart", "\"meta\": '['");
        patterns.setProperty("metaProperty", "'{'\"keyword\": \"{0}\", \"name\": \"{1}\", \"value\": \"{2}\"}");
        patterns.setProperty("metaEnd", "']'");
        patterns.setProperty("filter", "\"filter\": \"{0}\"");
        patterns.setProperty("narrative", "\"narrative\": '{'\"keyword\": \"{0}\",  \"inOrderTo\": '{'\"keyword\": \"{1}\", \"value\": \"{2}\"}, \"asA\": '{'\"keyword\": \"{3}\", \"value\": \"{4}\"}, \"iWantTo\": '{'\"keyword\": \"{5}\", \"value\": \"{6}\"}}");
        patterns.setProperty("lifecycleStart", "\"lifecycle\": '{'\"keyword\": \"{0}\"");
        patterns.setProperty("lifecycleEnd", "}");
        patterns.setProperty("lifecycleBeforeStart", "\"before\": '{'\"keyword\": \"{0}\", \"scopes\": [");
        patterns.setProperty("lifecycleBeforeEnd", "]}");
        patterns.setProperty("lifecycleAfterStart", "\"after\": '{'\"keyword\": \"{0}\", \"scopes\": [");
        patterns.setProperty("lifecycleAfterEnd", "]}");
        patterns.setProperty("lifecycleBeforeScopeStart", "'{'\"keyword\": \"{0}\", \"value\": \"{1}\", \"steps\": [");
        patterns.setProperty("lifecycleBeforeScopeEnd", "]}");
        patterns.setProperty("lifecycleAfterScopeStart", "'{'\"keyword\": \"{0}\", \"value\": \"{1}\", \"outcomes\": [");
        patterns.setProperty("lifecycleAfterScopeEnd", "]}");
        patterns.setProperty("lifecycleOutcomeStart", "'{'\"keyword\": \"{0}\", \"value\": \"{1}\", \"steps\": [");
        patterns.setProperty("lifecycleOutcomeEnd", "]}");
        patterns.setProperty("lifecycleMetaFilter", "\"metaFilter\": \"{0} {1}\"");
        patterns.setProperty("lifecycleStep", "\"{0}\"");
        patterns.setProperty("beforeBeforeStorySteps", "\"beforeStorySteps\": [");
        patterns.setProperty("afterBeforeStorySteps", "]");
        patterns.setProperty("beforeAfterStorySteps", "\"afterStorySteps\": [");
        patterns.setProperty("afterAfterStorySteps", "]");
        patterns.setProperty("beforeBeforeScenarioSteps", "\"beforeScenarioSteps\": [");
        patterns.setProperty("afterBeforeScenarioSteps", "]");
        patterns.setProperty("beforeAfterScenarioSteps", "\"afterScenarioSteps\": [");
        patterns.setProperty("afterAfterScenarioSteps", "]");
        patterns.setProperty("beforeScenarioSteps", "\"steps\": [");
        patterns.setProperty("afterScenarioSteps", "]");
        patterns.setProperty("beforeScenario","'{'\"keyword\": \"{0}\", \"title\": \"{1}\"");
        patterns.setProperty("scenarioNotAllowed", "\"notAllowed\": '{'\"pattern\": \"{0}\"}");
        patterns.setProperty("afterScenario", "}");
        patterns.setProperty("afterScenarioWithFailure", "\"failure\": \"{0}\" }");
        patterns.setProperty("givenStories", "\"givenStories\": '{'\"keyword\": \"{0}\", \"paths\": \"{1}\"}");
        patterns.setProperty("beforeGivenStories", "\"givenStories\": '{'");
        patterns.setProperty("givenStoriesStart", "\"keyword\": \"{0}\", \"givenStories\":[");
        patterns.setProperty("givenStory", "'{'\"parameters\": \"{1}\", \"path\": \"{0}\"}");
        patterns.setProperty("givenStoriesEnd", "]");
        patterns.setProperty("afterGivenStories", "}");
        patterns.setProperty("successful", "'{'\"outcome\": \"successful\", \"value\": \"{0}\"}");
        patterns.setProperty("ignorable", "'{'\"outcome\": \"ignorable\", \"value\": \"{0}\"}");
        patterns.setProperty("comment", "'{'\"outcome\": \"comment\", \"value\": \"{0}\"}");
        patterns.setProperty("pending", "'{'\"outcome\": \"pending\", \"keyword\": \"{1}\", \"value\": \"{0}\"}");
        patterns.setProperty("notPerformed", "'{'\"outcome\": \"notPerformed\", \"keyword\": \"{1}\", \"value\": \"{0}\"}");
        patterns.setProperty("failed", "'{'\"outcome\": \"failed\", \"keyword\": \"{1}\", \"value\": \"{0}\", \"failure\": \"{2}\"}");
        patterns.setProperty("restarted", "'{'\"outcome\": \"restarted\", \"value\": \"{0}\", \"reason\": \"{1}\"}");
        patterns.setProperty("restartedStory", "'{'\"story\": '{'\"outcome\": \"restartedStory\", \"value\": \"{0}\", \"reason\": \"{1}\"}}");
        patterns.setProperty("outcomesTableStart", "'{'\"outcomes\": '{'");
        patterns.setProperty("outcomesTableHeadStart", "\"fields\": [");
        patterns.setProperty("outcomesTableHeadCell", "\"{0}\"");
        patterns.setProperty("outcomesTableHeadEnd", "]");
        patterns.setProperty("outcomesTableBodyStart", "\"values\": [");
        patterns.setProperty("outcomesTableRowStart", "[");
        patterns.setProperty("outcomesTableCell", "\"{0}\"");
        patterns.setProperty("outcomesTableRowEnd", "]");
        patterns.setProperty("outcomesTableBodyEnd", "]");
        patterns.setProperty("outcomesTableEnd", "}}");
        patterns.setProperty("beforeExampleParameters", " \"parameters\": '{'");
        patterns.setProperty("afterExampleParameters", "}");
        patterns.setProperty("exampleParameter", "\"{0}\":\"{1}\"");
        patterns.setProperty("numericParameter", "\"{0}\":{1}");
        patterns.setProperty("beforeExamples", "\"examples\": '{'\"keyword\": \"{0}\"");
        patterns.setProperty("examplesStepsStart", "\"steps\": [");
        patterns.setProperty("examplesStep", "\"{0}\"");
        patterns.setProperty("examplesStepsEnd", "]");
        patterns.setProperty("afterExamples", "}");
        patterns.setProperty("examplesTableStart", "\"parameters\": '{'");
        patterns.setProperty("examplesTableHeadStart", "\"names\": [");
        patterns.setProperty("examplesTableHeadCell", "\"{0}\"");
        patterns.setProperty("examplesTableHeadEnd", "]");
        patterns.setProperty("examplesTableBodyStart", "\"values\": [");
        patterns.setProperty("examplesTableRowStart", "[");
        patterns.setProperty("examplesTableCell", "\"{0}\"");
        patterns.setProperty("examplesTableRowEnd", "]");
        patterns.setProperty("examplesTableBodyEnd", "]");
        patterns.setProperty("examplesTableEnd", "}");
        patterns.setProperty("exampleScenariosStart", "\"examples\": [");
        patterns.setProperty("exampleScenariosEnd", "]");
        patterns.setProperty("example", "'{'\"keyword\": \"{0}\"");
        patterns.setProperty("parameterVerbatimStart", "[[");
        patterns.setProperty("parameterVerbatimEnd", "]]");
        patterns.setProperty("parameterValueStart", "((");
        patterns.setProperty("parameterValueEnd", "))");
        patterns.setProperty("parameterValueNewline", "\\n");
        return patterns;
    }
}
