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
        super(TXT, output, mergeWithDefault(outputPatterns), keywords, reportFailureTrace);
    }

    private static Properties mergeWithDefault(Properties outputPatterns) {
        Properties patterns = defaultPatterns();
        // override any default pattern
        patterns.putAll(outputPatterns);
        return patterns;
    }

    private static Properties defaultPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("metaStart", "{0}\n");
        patterns.setProperty("metaProperty", "{0}{1} {2}\n");    
        patterns.setProperty("metaEnd", "\n");
        patterns.setProperty("successful", "{0}\n");
        patterns.setProperty("ignorable", "{0}\n");
        patterns.setProperty("pending", "{0} ({1})\n");
        patterns.setProperty("notPerformed", "{0} ({1})\n");
        patterns.setProperty("failed", "{0} ({1})\n({2})\n");
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
        patterns.setProperty("beforeStory", "{0}\n({1})\n");
        patterns.setProperty("narrative", "{0}\n{1} {2}\n{3} {4}\n{5} {6}\n");
        patterns.setProperty("afterStory", "\n");
        patterns.setProperty("beforeScenario", "{0} {1}\n");
        patterns.setProperty("afterScenario", "\n");
        patterns.setProperty("afterScenarioWithFailure", "\n{0}\n");
        patterns.setProperty("givenStories", "{0} {1}\n");
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
        patterns.setProperty("examplesTableBodyEnd", "\n");
        patterns.setProperty("examplesTableEnd", "\n");
        patterns.setProperty("example", "\n{0} {1}\n");
        patterns.setProperty("parameterValueStart", "");
        patterns.setProperty("parameterValueEnd", "");
        patterns.setProperty("parameterValueNewline", "\n");        
        patterns.setProperty("scenarioNotAllowed", "{0} {1} {2} {3}\n");
        patterns.setProperty("storyNotAllowed", "{0} {1} {2} {3}\n");
        return patterns;
    }


}
