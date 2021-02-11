package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.text.CaseUtils;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.failures.KnownFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.*;
import org.jbehave.core.model.OutcomesTable.Outcome;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Timing;

import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.jbehave.core.steps.StepCreator.*;

/**
 * <p>
 * Abstract story reporter that outputs to a PrintStream.
 * </p>
 * <p>
 * The output of the reported event is configurable via:
 * <ul>
 * <li>custom output patterns, providing only the patterns that differ from
 * default</li>
 * <li>keywords localised for different languages, providing the formatOutcome Locale</li>
 * <li>flag to report failure trace</li>
 * </ul>
 * </p>
 * <p>
 * Let's look at example of providing custom output patterns, e.g. for the
 * failed event. <br/>
 * we'd need to provide the custom pattern, say we want to have something like
 * "(step being executed) <<< FAILED", keyed on the method name:
 * 
 * <pre>
 * Properties patterns = new Properties();
 * patterns.setProperty(&quot;failed&quot;, &quot;{0} &lt;&lt;&lt; {1}&quot;);
 * </pre>
 * 
 * The pattern is by default processed and formatted by the
 * {@link MessageFormat}. Both the
 * {@link #format(String key, String defaultPattern, Object... args)} and
 * {@link #lookupPattern(String key, String defaultPattern)} methods are
 * override-able and a different formatter or pattern lookup can be used by
 * subclasses.
 * </p>
 * <p>
 * If the keyword "FAILED" (or any other keyword used by the reporter) needs to
 * be expressed in a different language, all we need to do is to provide an
 * instance of {@link org.jbehave.core.i18n.LocalizedKeywords} using the
 * appropriate {@link Locale}, e.g.
 * 
 * <pre>
 * Keywords keywords = new LocalizedKeywords(new Locale(&quot;it&quot;));
 * </pre>
 * 
 * </p>
 */
public abstract class PrintStreamOutput extends NullStoryReporter {

    private static final String EMPTY = "";
    public static final String NL = "\n";

    public enum Format {
        TXT {
            @Override
            public Object escapeValue(Object object) {
                return object;
            }
        },
        HTML {
            @Override
            public Object escapeValue(Object object) {
                return EscapeMode.HTML.escapeString(asString(object));
            }
        },
        XML {
            @Override
            public Object escapeValue(Object object) { return EscapeMode.XML.escapeString(asString(object)); }
        },
        JSON {
            @Override
            public Object escapeValue(Object object) {
                return EscapeMode.JSON.escapeString(asString(object));
            }
        };

        public abstract Object escapeValue(Object object);

        private static String asString(Object object) {
            return object != null ? object.toString() : EMPTY;
        }
    }

    private final Format format;
    private final PrintStream output;
    private final Properties outputPatterns;
    private final Keywords keywords;
    private ThreadLocal<Boolean> reportFailureTrace = new ThreadLocal<>();
    private ThreadLocal<Boolean> compressFailureTrace = new ThreadLocal<>();
    private ThreadLocal<Throwable> cause = new ThreadLocal<>();
    private ThreadLocal<Boolean> dryRun = ThreadLocal.withInitial(() -> false);

    protected PrintStreamOutput(Format format, PrintStream output, Properties defaultPatterns,
            Properties outputPatterns, Keywords keywords) {
        this(format, output, mergePatterns(defaultPatterns, outputPatterns), keywords, false,
                false);
    }

    protected PrintStreamOutput(Format format, PrintStream output, Properties defaultPatterns,
            Properties outputPatterns, Keywords keywords, boolean reportFailureTrace, boolean compressFailureTrace) {
        this(format, output, mergePatterns(defaultPatterns, outputPatterns), keywords, reportFailureTrace,
                compressFailureTrace);
    }

    protected PrintStreamOutput(Format format, PrintStream output, Properties outputPatterns, Keywords keywords,
            boolean reportFailureTrace, boolean compressFailureTrace) {
        this.format = format;
        this.output = output;
        this.outputPatterns = outputPatterns;
        this.keywords = keywords;
        doReportFailureTrace(reportFailureTrace);
        doCompressFailureTrace(compressFailureTrace);
    }

    private static Properties mergePatterns(Properties defaultPatterns, Properties outputPatterns) {
        Properties patterns = new Properties();
        patterns.putAll(defaultPatterns);
        // override any default pattern
        patterns.putAll(outputPatterns);
        return patterns;
    }

    @Override
    public void beforeStep(String step) {
        print(format("beforeStep", "{0}\n", step));
    }

    @Override
    public void successful(String step) {
        print(format("successful", "{0}\n", step));
    }

    @Override
    public void ignorable(String step) {
        print(format("ignorable", "{0}\n", step));
    }

    @Override
    public void comment(String step) {
        print(format("comment", "{0}\n", step));
    }

    @Override
    public void pending(String step) {
        print(format("pending", "{0} ({1})\n", step, keywords.pending()));
    }

    @Override
    public void notPerformed(String step) {
        print(format("notPerformed", "{0} ({1})\n", step, keywords.notPerformed()));
    }

    @Override
    public void failed(String step, Throwable storyFailure) {
        // storyFailure be used if a subclass has rewritten the "failed" pattern
        // to have a {3} as WebDriverHtmlOutput (jbehave-web) does.
        if (storyFailure instanceof UUIDExceptionWrapper) {
            this.cause.set(storyFailure.getCause());
            print(format("failed", "{0} ({1})\n({2})\n", step, keywords.failed(), storyFailure.getCause(),
                    ((UUIDExceptionWrapper) storyFailure).getUUID()));
        } else {
            throw new ClassCastException(storyFailure + " should be an instance of UUIDExceptionWrapper");
        }
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        failed(step, table.failureCause());
        print(table);
    }

    private void print(OutcomesTable table) {
        print(format("outcomesTableStart", NL));
        List<Outcome<?>> rows = table.getOutcomes();
        print(format("outcomesTableHeadStart", "|"));
        for (String field : table.getOutcomeFields()) {
            print(format("outcomesTableHeadCell", "{0}|", field));
        }
        print(format("outcomesTableHeadEnd", NL));
        print(format("outcomesTableBodyStart", EMPTY));
        for (Outcome<?> outcome : rows) {
            print(format("outcomesTableRowStart", "|", outcome.isVerified() ? "verified" : "notVerified"));
            print(format("outcomesTableCell", "{0}|", outcome.getDescription()));
            print(format("outcomesTableCell", "{0}|", renderOutcomeValue(outcome.getValue(), table)));
            print(format("outcomesTableCell", "{0}|", outcome.getMatcher()));
            print(format("outcomesTableCell", "{0}|", (outcome.isVerified() ? keywords.yes() : keywords.no())));
            print(format("outcomesTableRowEnd", NL));
        }
        print(format("outcomesTableBodyEnd", NL));
        print(format("outcomesTableEnd", NL));
    }

    private Object renderOutcomeValue(Object value, OutcomesTable outcomesTable) {
        if (value instanceof Date) {
            return new SimpleDateFormat(outcomesTable.getFormat(Date.class)).format(value);
        } else {
            return value;
        }
    }

    @Override
    public void storyExcluded(Story story, String filter) {
        print(format("filter", "{0}\n", filter));
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        print(format("storyCancelled", "{0}: {1} ({2} s)\n", keywords.storyCancelled(), keywords.duration(),
                storyDuration.getDurationInSecs()));
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        print(format("beforeStory", "{0}\n{1}\n({2})\n", story.getId(), story.getDescription().asString(),
                story.getPath()));
        if (dryRun.get()) {
            print(format("dryRun", "{0}\n", keywords.dryRun()));
        }
        print(story.getMeta());
    }

    @Override
    public void beforeScenarios() {
        print(format("beforeScenarios", ""));
    }

    @Override
    public void afterScenarios() {
        print(format("afterScenarios", ""));
    }

    @Override
    public void narrative(Narrative narrative) {
        if (!narrative.isEmpty()) {
            if (!narrative.isAlternative()) {
                print(format("narrative", "{0}\n{1} {2}\n{3} {4}\n{5} {6}\n", keywords.narrative(),
                        keywords.inOrderTo(), narrative.inOrderTo(), keywords.asA(), narrative.asA(),
                        keywords.iWantTo(), narrative.iWantTo()));
            } else {
                print(format("narrative", "{0}\n{1} {2}\n{3} {4}\n{5} {6}\n", keywords.narrative(), keywords.asA(),
                        narrative.asA(), keywords.iWantTo(), narrative.iWantTo(), keywords.soThat(), narrative.soThat()));
            }
        }
    }

    @Override
    public void lifecycle(Lifecycle lifecycle) {
        if (!lifecycle.isEmpty()) {
            print(format("lifecycleStart", "{0}\n", keywords.lifecycle()));
            ExamplesTable lifecycleExamplesTable = lifecycle.getExamplesTable();
            if (!lifecycleExamplesTable.isEmpty()) {
                print(formatTable(lifecycleExamplesTable));
            }
            if (lifecycle.hasBeforeSteps()) {
                print(format("lifecycleBeforeStart", "{0}\n", keywords.before()));
                for (Scope scope : lifecycle.getScopes() ){
                    printWithScope(lifecycle.getBeforeSteps(scope), scope);
                }
                print(format("lifecycleBeforeEnd", NL));
            }
            if (lifecycle.hasAfterSteps()) {
                print(format("lifecycleAfterStart", "{0}\n", keywords.after()));
                for (Scope scope : lifecycle.getScopes() ){
                    printOutcomes(lifecycle, scope);
                }
                print(format("lifecycleAfterEnd", NL));
            }
            print(format("lifecycleEnd", NL));
        }
    }

    private void printOutcomes(Lifecycle lifecycle, Scope scope) {
        for ( AfterScenario.Outcome outcome : lifecycle.getOutcomes() ){
            List<String> afterSteps = lifecycle.getAfterSteps(scope, outcome);
            if ( !afterSteps.isEmpty() ) {
                print(format("lifecycleAfterScopeStart", "{0} {1}\n", keywords.scope(), formatScope(scope)));
                print(format("lifecycleOutcomeStart", "{0} {1}\n", keywords.outcome(), formatOutcome(outcome)));
                MetaFilter metaFilter = lifecycle.getMetaFilter(outcome);
                if (!metaFilter.isEmpty()) {
                    print(format("lifecycleMetaFilter", "{0} {1}\n", keywords.metaFilter(), metaFilter.asString()));
                }
                print(afterSteps);
                print(format("lifecycleOutcomeEnd", "\n"));
                print(format("lifecycleAfterScopeEnd", "\n"));
            }
        }
    }

    private void printWithScope(List<String> steps, Scope scope) {
        if ( !steps.isEmpty()) {
            print(format("lifecycleBeforeScopeStart", "{0} {1}\n", keywords.scope(), formatScope(scope)));
            print(steps);
            print(format("lifecycleBeforeScopeEnd", "\n"));
        }
    }

    private String formatScope(Scope scope) {
        switch ( scope ){
            case SCENARIO: return keywords.scopeScenario();
            case STORY: return keywords.scopeStory();
            default: return scope.name();
        }
    }

    private String formatOutcome(AfterScenario.Outcome outcome) {
        switch ( outcome ){
        case ANY: return keywords.outcomeAny();
        case SUCCESS: return keywords.outcomeSuccess();
        case FAILURE: return keywords.outcomeFailure();
        default: return outcome.name();
        }
    }

    private void print(List<String> steps) {
        for (String step : steps) {
            print(format("lifecycleStep", "{0}\n", step));
        }
    }

    private void print(Meta meta) {
        if (!meta.isEmpty()) {
            print(format("metaStart", "{0}\n", keywords.meta()));
            for (String name : meta.getPropertyNames()) {
                print(format("metaProperty", "{0}{1} {2}", keywords.metaProperty(), name, meta.getProperty(name)));
            }
            print(format("metaEnd", NL));
        }
    }

    @Override
    public void beforeScenarioSteps(Stage stage) {
        printScenarioSteps("before", stage);
    }

    @Override
    public void afterScenarioSteps(Stage stage) {
        printScenarioSteps("after", stage);
    }

    private void printScenarioSteps(String stepsStage, Stage stage) {
        printSteps(stepsStage, "Scenario", stage);
    }

    @Override
    public void beforeComposedSteps() {
        print(format("beforeComposedSteps", ""));
    }

    @Override
    public void afterComposedSteps() {
        print(format("afterComposedSteps", ""));
    }

    @Override
    public void beforeStoriesSteps(Stage stage) {
        printStoriesSteps("before", stage);
    }

    @Override
    public void afterStoriesSteps(Stage stage) {
        printStoriesSteps("after", stage);
    }

    private void printStoriesSteps(String stepsStage, Stage stage) {
        printSteps(stepsStage, "Stories", stage);
    }

    @Override
    public void beforeStorySteps(Stage stage) {
        printStorySteps("before", stage);
    }

    @Override
    public void afterStorySteps(Stage stage) {
        printStorySteps("after", stage);
    }

    private void printStorySteps(String stepsStage, Stage stage) {
        printSteps(stepsStage, "Story", stage);
    }

    private void printSteps(String stepsStage, String parent, Stage stage) {
        String stageName = stage != null ? CaseUtils.toCamelCase(stage.name(), true) : EMPTY;
        print(format(stepsStage + stageName + parent + "Steps", ""));
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        print(format("afterStory", NL));
        // take care not to close System.out
        // which is used for ConsoleOutput
        if (!givenOrRestartingStory && output != System.out) {
            output.close();
        }
    }

    @Override
    public void beforeGivenStories() {
        print(format("beforeGivenStories", ""));
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        print(format("givenStoriesStart", "{0}\n", keywords.givenStories()));
        for (GivenStory givenStory : givenStories.getStories()) {
            print(format("givenStory", "{0}{1}\n", givenStory.asString(),
                    (givenStory.hasAnchor() ? givenStory.getParameters() : "")));
        }
        print(format("givenStoriesEnd", NL));
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        givenStories(new GivenStories(StringUtils.join(storyPaths, ",")));
    }

    @Override
    public void afterGivenStories() {
        print(format("afterGivenStories", ""));
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter) {
        print(format("filter", "{0}\n", filter));
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        cause.set(null);
        print(format("beforeScenario", "{0} {1} {2}\n", scenario.getId(), keywords.scenario(), scenario.getTitle()));
        print(scenario.getMeta());
    }

    @Override
    public void afterScenario(Timing timing) {
        print(format("numericParameter", EMPTY, "start", timing.getStart()));
        print(format("numericParameter", EMPTY, "end", timing.getEnd()));
        if (cause.get() != null && !(cause.get() instanceof KnownFailure) && reportFailureTrace() ) {
            print(format("afterScenarioWithFailure", "\n{0}\n",
                    new StackTraceFormatter(compressFailureTrace()).stackTrace(cause.get())));
        } else {
            print(format("afterScenario", NL));
        }
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        print(format("beforeExamples", "{0}\n", keywords.examplesTable()));
        print(format("examplesStepsStart", EMPTY));
        for (String step : steps) {
            print(format("examplesStep", "{0}\n", step));
        }
        print(format("examplesStepsEnd", EMPTY));
        print(formatTable(table));
        print(format("exampleScenariosStart", EMPTY));
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        print(format("example", "\n{0} {1}\n", keywords.examplesTableRow(), tableRow));
        print(output, format("beforeExampleParameters", EMPTY));
        tableRow.entrySet().forEach(cell -> print(output, format("exampleParameter", EMPTY, cell.getKey(), cell.getValue())));
        print(output, format("afterExampleParameters", EMPTY));
    }

    @Override
    public void afterExamples() {
        print(format("exampleScenariosEnd", EMPTY));
        print(format("afterExamples", NL));
    }

    @Override
    public void dryRun() {
        dryRun.set(true);
    }

    @Override
    public void pendingMethods(List<String> methods) {
        print(format("pendingMethodsStart", EMPTY));
        for (String method : methods) {
            print(format("pendingMethod", "{0}\n", method));
        }
        print(format("pendingMethodsEnd", EMPTY));
    }

    @Override
    public void restarted(String step, Throwable cause) {
        print(format("restarted", "{0} {1}\n", step, cause.getMessage()));
    }
    
    @Override
    public void restartedStory(Story story, Throwable cause) {
        print(format("restartedStory", "{0} {1}\n", story.getPath(), cause.getMessage()));
    }

    /**
     * Formats event output by key, usually equal to the method name.
     * 
     * @param key the event key
     * @param defaultPattern the default pattern to return if a custom pattern
     *            is not found
     * @param args the args used to format output
     * @return A formatted event output
     */
    protected String format(String key, String defaultPattern, Object... args) {
        String escape = escape(defaultPattern);
        String s = lookupPattern(key, escape);
        Object[] objects = escapeAll(args);
        return MessageFormat.format(s, objects);
    }

    protected String formatTable(ExamplesTable table) {
        OutputStream formatted = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(formatted);
        print(out, format("examplesTableStart", NL));
        List<Map<String, String>> rows = table.getRows();
        List<String> headers = table.getHeaders();
        print(out, format("examplesTableHeadStart", "|"));
        for (String header : headers) {
            print(out, format("examplesTableHeadCell", "{0}|", header));
        }
        print(out, format("examplesTableHeadEnd", NL));
        print(out, format("examplesTableBodyStart", EMPTY));
        for (Map<String, String> row : rows) {
            print(out, format("examplesTableRowStart", "|"));
            for (String header : headers) {
                print(out, format("examplesTableCell", "{0}|", row.get(header)));
            }
            print(out, format("examplesTableRowEnd", NL));
        }
        print(out, format("examplesTableBodyEnd", EMPTY));
        print(out, format("examplesTableEnd", EMPTY));
        return formatted.toString();
    }

    protected String formatVerbatim(Verbatim verbatim) {
        OutputStream formatted = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(formatted);
        print(out, format("verbatimStart", NL));
        print(out, verbatim.getContent());
        print(out, format("verbatimEnd", NL));
        return formatted.toString();
    }

    private String escape(String defaultPattern) {
        return (String) escapeAll(defaultPattern)[0];
    }

    private Object[] escapeAll(Object... args) {
        return escape(format, args);
    }

    /**
     * Escapes args' string values according to format
     *
     * @param format the Format used by the PrintStream
     * @param args the array of args to escape
     * @return The cloned and escaped array of args
     */
    protected Object[] escape(final Format format, Object... args) {
        // Transformer that escapes HTML,XML,JSON strings
        return Stream.of(args).map(format::escapeValue).toArray();
    }

    /**
     * Looks up the format pattern for the event output by key, conventionally
     * equal to the method name. The pattern is used by the
     * {#format(String,String,Object...)} method and by default is formatted
     * using the {@link MessageFormat#format(String, Object...)} method. If no
     * pattern is found for key or needs to be overridden, the default pattern
     * should be returned.
     * 
     * @param key the format pattern key
     * @param defaultPattern the default pattern if no pattern is
     * @return The format patter for the given key
     */
    protected String lookupPattern(String key, String defaultPattern) {
        if (outputPatterns.containsKey(key)) {
            return outputPatterns.getProperty(key);
        }
        return defaultPattern;
    }

    public boolean reportFailureTrace() {
        Boolean reportFailure = reportFailureTrace.get();
        if ( reportFailure != null ){
            return reportFailure;
        }
        return false;
    }

    public PrintStreamOutput doReportFailureTrace(boolean reportFailureTrace) {
        this.reportFailureTrace.set(reportFailureTrace);
        return this;
    }

    public boolean compressFailureTrace() {
        return compressFailureTrace.get();
    }

    public PrintStreamOutput doCompressFailureTrace(boolean compressFailureTrace) {
        this.compressFailureTrace.set(compressFailureTrace);
        return this;
    }

    protected void overwritePattern(String key, String pattern) {
        outputPatterns.put(key, pattern);
    }

    /**
     * Prints text to output stream, replacing parameter start and end
     * placeholders
     * 
     * @param text the String to print
     */
    protected void print(String text) {
        String tableStart = format(PARAMETER_TABLE_START, PARAMETER_TABLE_START);
        String tableEnd = format(PARAMETER_TABLE_END, PARAMETER_TABLE_END);
        boolean containsTable = text.contains(tableStart) && text.contains(tableEnd);
        String verbatimStart = format(PARAMETER_VERBATIM_START, PARAMETER_VERBATIM_START);
        String verbatimEnd = format(PARAMETER_VERBATIM_END, PARAMETER_VERBATIM_END);
        boolean containsVerbatim = text.contains(verbatimStart) && text.contains(verbatimEnd);
        String textToPrint;
        if ( containsTable ) {
            textToPrint = transformPrintingTable(text, tableStart, tableEnd);
        } else if ( containsVerbatim ){
            textToPrint = transformPrintingVerbatim(text, verbatimStart, verbatimEnd);
        } else {
            textToPrint = text;
        }
        print(output, textToPrint
                .replace(format(PARAMETER_VALUE_START, PARAMETER_VALUE_START), format("parameterValueStart", EMPTY))
                .replace(format(PARAMETER_VALUE_END, PARAMETER_VALUE_END), format("parameterValueEnd", EMPTY))
                .replace(format(PARAMETER_VALUE_NEWLINE, PARAMETER_VALUE_NEWLINE), format("parameterValueNewline", NL)));
    }

    protected String transformPrintingTable(String text, String tableStart, String tableEnd) {
        String tableAsString = substringBetween(text, tableStart, tableEnd);
        return text
                .replace(tableAsString, formatTable(new ExamplesTable(tableAsString)))
                .replace(tableStart, format("parameterValueStart", EMPTY))
                .replace(tableEnd, format("parameterValueEnd", EMPTY));
    }

    protected String  transformPrintingVerbatim(String text, String verbatimStart, String verbatimEnd) {
        String verbatimAsString = substringBetween(text, verbatimStart, verbatimEnd);
        return text
                .replace(verbatimAsString, formatVerbatim(new Verbatim(verbatimAsString)))
                .replace(verbatimStart, format("parameterValueStart", EMPTY))
                .replace(verbatimEnd, format("parameterValueEnd", EMPTY));
    }

    protected void print(PrintStream output, String text) {
        output.print(text);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(format).append(output).toString();
    }
}
