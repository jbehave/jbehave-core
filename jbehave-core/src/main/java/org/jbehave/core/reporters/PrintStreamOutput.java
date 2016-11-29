package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.failures.KnownFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.Outcome;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.jbehave.core.steps.StepCreator.PARAMETER_TABLE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_TABLE_START;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_NEWLINE;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

/**
 * <p>
 * Abstract story reporter that outputs to a PrintStream.
 * </p>
 * <p>
 * The output of the reported event is configurable via:
 * <ul>
 * <li>custom output patterns, providing only the patterns that differ from
 * default</li>
 * <li>keywords localised for different languages, providing the i18n Locale</li>
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
public abstract class PrintStreamOutput implements StoryReporter {

    private static final String EMPTY = "";

    public enum Format {
        TXT, HTML, XML
    }

    private final Format format;
    private final PrintStream output;
    private final Properties outputPatterns;
    private final Keywords keywords;
    private ThreadLocal<Boolean> reportFailureTrace = new ThreadLocal<Boolean>();
    private ThreadLocal<Boolean> compressFailureTrace = new ThreadLocal<Boolean>();
    private ThreadLocal<Throwable> cause = new ThreadLocal<Throwable>();

    protected PrintStreamOutput(Format format, PrintStream output, Properties outputPatterns, Keywords keywords,
            boolean reportFailureTrace, boolean compressFailureTrace) {
        this.format = format;
        this.output = output;
        this.outputPatterns = outputPatterns;
        this.keywords = keywords;
        doReportFailureTrace(reportFailureTrace);
        doCompressFailureTrace(compressFailureTrace);
    }

    @Override
    public void beforeStep(String step) {
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
        print(format("outcomesTableStart", "\n"));
        List<Outcome<?>> rows = table.getOutcomes();
        print(format("outcomesTableHeadStart", "|"));
        for (String field : table.getOutcomeFields()) {
            print(format("outcomesTableHeadCell", "{0}|", field));
        }
        print(format("outcomesTableHeadEnd", "\n"));
        print(format("outcomesTableBodyStart", EMPTY));
        for (Outcome<?> outcome : rows) {
            print(format("outcomesTableRowStart", "|", outcome.isVerified() ? "verified" : "notVerified"));
            print(format("outcomesTableCell", "{0}|", outcome.getDescription()));
            print(format("outcomesTableCell", "{0}|", renderOutcomeValue(outcome.getValue(), table.getDateFormat())));
            print(format("outcomesTableCell", "{0}|", outcome.getMatcher()));
            print(format("outcomesTableCell", "{0}|", (outcome.isVerified() ? keywords.yes() : keywords.no())));
            print(format("outcomesTableRowEnd", "\n"));
        }
        print(format("outcomesTableBodyEnd", "\n"));
        print(format("outcomesTableEnd", "\n"));
    }

    private Object renderOutcomeValue(Object value, String dateFormat) {
        if (value instanceof Date) {
            return new SimpleDateFormat(dateFormat).format(value);
        } else {
            return value;
        }
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        print(format("filter", "{0}\n", filter));
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        print(format("storyCancelled", "{0}: {1} ({2} s)\n", keywords.storyCancelled(), keywords.duration(),
                storyDuration.getDurationInSecs()));
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        print(format("beforeStory", "{0}\n({1})\n", story.getDescription().asString(), story.getPath()));
        if (!story.getMeta().isEmpty()) {
            Meta meta = story.getMeta();
            print(meta);
        }
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
    public void lifecyle(Lifecycle lifecycle) {
        if (!lifecycle.isEmpty()) {
            print(format("lifecycleStart", "{0}\n", keywords.lifecycle()));
            if (!lifecycle.getBeforeSteps().isEmpty()) {
                print(format("lifecycleBeforeStart", "{0}\n", keywords.before()));
                print(lifecycle.getBeforeSteps());
                print(format("lifecycleBeforeEnd", ""));
            }
            if (!lifecycle.getAfterSteps().isEmpty()) {
                print(format("lifecycleAfterStart", "{0}\n", keywords.after()));
                for ( org.jbehave.core.annotations.AfterScenario.Outcome outcome : lifecycle.getOutcomes() ){
                    print(format("lifecycleOutcome", "{0} {1}\n", keywords.outcome(), i18n(outcome)));
                    MetaFilter metaFilter = lifecycle.getMetaFilter(outcome);
                    if ( !metaFilter.isEmpty() ){
                        print(format("lifecycleMetaFilter", "{0} {1}\n", keywords.metaFilter(), metaFilter.asString()));
                    }
                    print(lifecycle.getAfterSteps(outcome));
                }
                print(format("lifecycleAfterEnd", ""));
            }
            print(format("lifecycleEnd", "\n"));
        }
    }

    private String i18n(org.jbehave.core.annotations.AfterScenario.Outcome outcome) {
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
        print(format("metaStart", "{0}\n", keywords.meta()));
        for (String name : meta.getPropertyNames()) {
            print(format("metaProperty", "{0}{1} {2}", keywords.metaProperty(), name, meta.getProperty(name)));
        }
        print(format("metaEnd", "\n"));
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        print(format("afterStory", "\n"));
        // take care not to close System.out
        // which is used for ConsoleOutput
        if (!givenOrRestartingStory && output != System.out) {
            output.close();
        }
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        print(format("givenStoriesStart", "{0}\n", keywords.givenStories()));
        for (GivenStory givenStory : givenStories.getStories()) {
            print(format("givenStory", "{0} {1}\n", givenStory.asString(),
                    (givenStory.hasAnchor() ? givenStory.getParameters() : "")));
        }
        print(format("givenStoriesEnd", "\n"));
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        givenStories(new GivenStories(StringUtils.join(storyPaths, ",")));
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        print(format("filter", "{0}\n", filter));
    }

    @Override
    public void beforeScenario(String title) {
        cause.set(null);
        print(format("beforeScenario", "{0} {1}\n", keywords.scenario(), title));
    }

    @Override
    public void scenarioMeta(Meta meta) {
        if (!meta.isEmpty()) {
            print(meta);
        }
    }

    @Override
    public void afterScenario() {
        if (cause.get() != null && !(cause.get() instanceof KnownFailure) && reportFailureTrace() ) {
            print(format("afterScenarioWithFailure", "\n{0}\n",
                    new StackTraceFormatter(compressFailureTrace()).stackTrace(cause.get())));
        } else {
            print(format("afterScenario", "\n"));
        }
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        print(format("beforeExamples", "{0}\n", keywords.examplesTable()));
        for (String step : steps) {
            print(format("examplesStep", "{0}\n", step));
        }
        print(formatTable(table));
    }

    @Override
    public void example(Map<String, String> tableRow) {
        print(format("example", "\n{0} {1}\n", keywords.examplesTableRow(), tableRow));
    }

    @Override
    public void afterExamples() {
        print(format("afterExamples", "\n"));
    }

    @Override
    public void dryRun() {
        print(format("dryRun", "{0}\n", keywords.dryRun()));
    }

    @Override
    public void pendingMethods(List<String> methods) {
        for (String method : methods) {
            print(format("pendingMethod", "{0}\n", method));
        }
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
        out.print(format("examplesTableStart", "\n"));
        List<Map<String, String>> rows = table.getRows();
        List<String> headers = table.getHeaders();
        out.print(format("examplesTableHeadStart", "|"));
        for (String header : headers) {
            out.print(format("examplesTableHeadCell", "{0}|", header));
        }
        out.print(format("examplesTableHeadEnd", "\n"));
        out.print(format("examplesTableBodyStart", EMPTY));
        for (Map<String, String> row : rows) {
            out.print(format("examplesTableRowStart", "|"));
            for (String header : headers) {
                out.print(format("examplesTableCell", "{0}|", row.get(header)));
            }
            out.print(format("examplesTableRowEnd", "\n"));
        }
        out.print(format("examplesTableBodyEnd", ""));
        out.print(format("examplesTableEnd", ""));
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
        // Transformer that escapes HTML and XML strings
        Transformer<Object, Object> escapingTransformer = new Transformer<Object, Object>() {
            @Override
            public Object transform(Object object) {
                switch (format) {
                case HTML:
                    return escapeHtml4(asString(object));
                case XML:
                    return escapeXml(asString(object));
                default:
                    return object;
                }
            }

            private String asString(Object object) {
                return (object != null ? object.toString() : EMPTY);
            }
        };
        List<Object> list = Arrays.asList(ArrayUtils.clone(args));
        CollectionUtils.transform(list, escapingTransformer);
        return list.toArray();
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
        if (containsTable(text)) {
            String tableStart = format(PARAMETER_TABLE_START, PARAMETER_TABLE_START);
            String tableEnd = format(PARAMETER_TABLE_END, PARAMETER_TABLE_END);
            String tableAsString = substringBetween(text, tableStart, tableEnd);
            output.print(text
                    .replace(tableAsString, formatTable(new ExamplesTable(tableAsString)))
                    .replace(tableStart, format("parameterValueStart", EMPTY))
                    .replace(tableEnd, format("parameterValueEnd", EMPTY))
                    .replace(format(PARAMETER_VALUE_START, PARAMETER_VALUE_START), format("parameterValueStart", EMPTY))
                    .replace(format(PARAMETER_VALUE_END, PARAMETER_VALUE_END), format("parameterValueEnd", EMPTY))
                    .replace(format(PARAMETER_VALUE_NEWLINE, PARAMETER_VALUE_NEWLINE),
                            format("parameterValueNewline", "\n")));
        } else {
            output.print(text
                    .replace(format(PARAMETER_VALUE_START, PARAMETER_VALUE_START), format("parameterValueStart", EMPTY))
                    .replace(format(PARAMETER_VALUE_END, PARAMETER_VALUE_END), format("parameterValueEnd", EMPTY))
                    .replace(format(PARAMETER_VALUE_NEWLINE, PARAMETER_VALUE_NEWLINE),
                            format("parameterValueNewline", "\n")));
        }
    }

    private boolean containsTable(String text) {
        String tableStart = format(PARAMETER_TABLE_START, PARAMETER_TABLE_START);
        String tableEnd = format(PARAMETER_TABLE_END, PARAMETER_TABLE_END);
        return text.contains(tableStart) && text.contains(tableEnd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(format).append(output).toString();
    }
}
