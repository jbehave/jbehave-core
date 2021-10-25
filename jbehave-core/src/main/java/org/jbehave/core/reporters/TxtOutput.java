package org.jbehave.core.reporters;

import static org.jbehave.core.reporters.PrintStreamOutput.Format.TXT;

import java.io.PrintStream;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;

/**
 * <p>
 * Story reporter that outputs to a PrintStream, as TXT. It extends
 * {@link PrintStreamOutput}, providing TXT-based default output
 * patterns, which can be overridden via the {@link
 * TxtOutput (PrintStream,Properties)} constructor.
 * </p>
 * 
 * @author Mauro Talevi
 */
public class TxtOutput extends PrintStreamOutput {

    public TxtOutput(PrintStream output) {
        this(output, new Properties());
    }

    public TxtOutput(PrintStream output, Properties outputPatterns) {
        this(output, outputPatterns, new LocalizedKeywords());
    }

    public TxtOutput(PrintStream output, Keywords keywords) {
        this(output, new Properties(), keywords);
    }

    public TxtOutput(PrintStream output, Properties outputPatterns, Keywords keywords) {
        this(output, outputPatterns, keywords, false);
    }

    public TxtOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportFailureTrace) {
        this(output, outputPatterns, keywords, reportFailureTrace, false);
    }

    public TxtOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportFailureTrace, boolean compressFailureTrace) {
        super(TXT, output, defaultPatterns(), outputPatterns, keywords, reportFailureTrace, compressFailureTrace);
    }

    private static Properties defaultPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("beforeStep", "");
        patterns.setProperty("dryRun", "{0}\n");
        patterns.setProperty("storyCancelled", "{0} ({1} {2} s)\n");
        patterns.setProperty("beforeStory", "{1}\n({2})\n");
        patterns.setProperty("afterStory", "\n");
        patterns.setProperty("metaStart", "{0}\n");
        patterns.setProperty("metaProperty", "{0}{1} {2}\n");    
        patterns.setProperty("metaEnd", "\n");
        patterns.setProperty("filter", "{0}\n");
        patterns.setProperty("narrative", "{0}\n{1} {2}\n{3} {4}\n{5} {6}\n");
        patterns.setProperty("lifecycleStart", "{0}\n");
        patterns.setProperty("lifecycleEnd", "\n");
        patterns.setProperty("lifecycleBeforeStart", "{0}\n");
        patterns.setProperty("lifecycleBeforeEnd", "\n");
        patterns.setProperty("lifecycleAfterStart", "{0}\n");
        patterns.setProperty("lifecycleAfterEnd", "\n");
        patterns.setProperty("lifecycleBeforeScopeStart", "{0} {1}\n");
        patterns.setProperty("lifecycleBeforeScopeEnd", "\n");
        patterns.setProperty("lifecycleAfterScopeStart", "{0} {1}\n");
        patterns.setProperty("lifecycleAfterScopeEnd", "\n");
        patterns.setProperty("lifecycleOutcomeStart", "{0} {1}\n");
        patterns.setProperty("lifecycleOutcomeEnd", "\n");
        patterns.setProperty("lifecycleMetaFilter", "{0} {1}\n");
        patterns.setProperty("lifecycleStep", "{0}\n");
        patterns.setProperty("beforeBeforeStoriesSteps", "BeforeStories\n");
        patterns.setProperty("afterBeforeStoriesSteps", "\n");
        patterns.setProperty("beforeAfterStoriesSteps", "AfterStories\n");
        patterns.setProperty("afterAfterStoriesSteps", "\n");
        patterns.setProperty("beforeBeforeSystemStorySteps", "BeforeSystemStorySteps\n");
        patterns.setProperty("afterBeforeSystemStorySteps", "\n");
        patterns.setProperty("beforeAfterSystemStorySteps", "AfterSystemStorySteps\n");
        patterns.setProperty("afterAfterSystemStorySteps", "\n");
        patterns.setProperty("beforeBeforeUserStorySteps", "BeforeUserStorySteps\n");
        patterns.setProperty("afterBeforeUserStorySteps", "\n");
        patterns.setProperty("beforeAfterUserStorySteps", "AfterUserStorySteps\n");
        patterns.setProperty("afterAfterUserStorySteps", "\n");
        patterns.setProperty("beforeBeforeSystemScenarioSteps", "BeforeSystemScenarioSteps\n");
        patterns.setProperty("afterBeforeSystemScenarioSteps", "\n");
        patterns.setProperty("beforeAfterSystemScenarioSteps", "AfterSystemScenarioSteps\n");
        patterns.setProperty("afterAfterSystemScenarioSteps", "\n");
        patterns.setProperty("beforeBeforeUserScenarioSteps", "BeforeUserScenarioSteps\n");
        patterns.setProperty("afterBeforeUserScenarioSteps", "\n");
        patterns.setProperty("beforeAfterUserScenarioSteps", "AfterUserScenarioSteps\n");
        patterns.setProperty("afterAfterUserScenarioSteps", "\n");
        patterns.setProperty("beforeScenario", "{1} {2}\n");
        patterns.setProperty("afterScenario", "\n");
        patterns.setProperty("afterScenarioWithFailure", "\n{0}\n");
        patterns.setProperty("givenStories", "{0} {1}\n");
        patterns.setProperty("givenStoriesStart", "{0}\n");
        patterns.setProperty("givenStory", "{0}{1}\n");
        patterns.setProperty("givenStoriesEnd", "\n");
        patterns.setProperty("successful", "{0}\n");
        patterns.setProperty("ignorable", "{0}\n");
        patterns.setProperty("comment", "{0}\n");
        patterns.setProperty("pending", "{0} ({1})\n");
        patterns.setProperty("notPerformed", "{0} ({1})\n");
        patterns.setProperty("failed", "{0} ({1})\n({2})\n");
        patterns.setProperty("restarted", "{0} ({1})\n");
        patterns.setProperty("restartedStory", "{0} ({1})\n");
        patterns.setProperty("outcomesTableStart", "");
        patterns.setProperty("outcomesTableHeadStart", "|");
        patterns.setProperty("outcomesTableHeadCell", "{0}|");
        patterns.setProperty("outcomesTableHeadEnd", "\n");
        patterns.setProperty("outcomesTableBodyStart", "");
        patterns.setProperty("outcomesTableRowStart", "|");
        patterns.setProperty("outcomesTableCell", "{0}|");
        patterns.setProperty("outcomesTableRowEnd", "\n");
        patterns.setProperty("outcomesTableBodyEnd", "");
        patterns.setProperty("outcomesTableEnd", "");
        patterns.setProperty("beforeExamples", "{0}\n");
        patterns.setProperty("examplesStep", "{0}\n");
        patterns.setProperty("afterExamples", "\n");
        patterns.setProperty("examplesTableStart", "\n");
        patterns.setProperty("examplesTableHeadStart", "|");
        patterns.setProperty("examplesTableHeadCell", "{0}|");
        patterns.setProperty("examplesTableHeadEnd", "\n");
        patterns.setProperty("examplesTableBodyStart", "");
        patterns.setProperty("examplesTableRowStart", "|");
        patterns.setProperty("examplesTableCell", "{0}|");
        patterns.setProperty("examplesTableRowEnd", "\n");
        patterns.setProperty("examplesTableBodyEnd", "");
        patterns.setProperty("examplesTableEnd", "");
        patterns.setProperty("example", "\n{0} {1}\n");
        patterns.setProperty("parameterValueStart", "");
        patterns.setProperty("parameterValueEnd", "");
        patterns.setProperty("parameterValueNewline", "\n");     
        return patterns;
    }
}
