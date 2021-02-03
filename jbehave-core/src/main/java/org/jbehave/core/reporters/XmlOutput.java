package org.jbehave.core.reporters;

import java.io.PrintStream;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;

import static org.jbehave.core.reporters.PrintStreamOutput.Format.XML;

/**
 * <p>
 * Story reporter that outputs to a PrintStream, as XML. It extends
 * {@link PrintStreamOutput}, providing XML-based default output
 * patterns, which can be overridden via the {@link
 * XmlOutput (PrintStream,Properties)} constructor.
 * </p>
 * 
 * @author Mauro Talevi
 */
public class XmlOutput extends PrintStreamOutput {

    public XmlOutput(PrintStream output) {
        this(output, new Properties());
    }

    public XmlOutput(PrintStream output, Properties outputPatterns) {
        this(output, outputPatterns, new LocalizedKeywords());
    }
    
    public XmlOutput(PrintStream output, Keywords keywords) {
        this(output, new Properties(), keywords);
    }

    public XmlOutput(PrintStream output, Properties outputPatterns, Keywords keywords) {
        this(output, outputPatterns, keywords, false);
    }

    public XmlOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportFailureTrace) {
        this(output, outputPatterns, keywords, reportFailureTrace, false);
    }

    public XmlOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportFailureTrace, boolean compressFailureTrace) {
        super(XML, output, defaultXmlPatterns(), outputPatterns, keywords, reportFailureTrace, compressFailureTrace);
    }

    private static Properties defaultXmlPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("dryRun", "<dryRun>{0}</dryRun>\n");        
        patterns.setProperty("beforeStory", "<story path=\"{1}\" title=\"{0}\">\n");
        patterns.setProperty("storyCancelled", "<cancelled keyword=\"{0}\" durationKeyword=\"{1}\" durationInSecs=\"{2}\"/>\n");
        patterns.setProperty("afterStory", "</story>\n");
        patterns.setProperty("pendingMethod", "<pendingMethod>{0}</pendingMethod>\n");        
        patterns.setProperty("metaStart", "<meta>\n");
        patterns.setProperty("metaProperty", "<property keyword=\"{0}\" name=\"{1}\" value=\"{2}\"/>\n");        
        patterns.setProperty("metaEnd", "</meta>\n");
        patterns.setProperty("filter", "<filter>{0}</filter>\n");        
        patterns.setProperty("narrative", "<narrative keyword=\"{0}\">\n  <inOrderTo keyword=\"{1}\">{2}</inOrderTo>\n  <asA keyword=\"{3}\">{4}</asA>\n  <iWantTo keyword=\"{5}\">{6}</iWantTo>\n</narrative>\n");
        patterns.setProperty("lifecycleStart", "<lifecycle keyword=\"{0}\">\n");
        patterns.setProperty("lifecycleEnd", "</lifecycle>\n");
        patterns.setProperty("lifecycleBeforeStart", "<before keyword=\"{0}\">\n");
        patterns.setProperty("lifecycleBeforeEnd", "</before>\n");        
        patterns.setProperty("lifecycleAfterStart", "<after keyword=\"{0}\">\n");
        patterns.setProperty("lifecycleAfterEnd", "</after>\n");
        patterns.setProperty("lifecycleBeforeScopeStart", "<scope keyword=\"{0}\" value=\"{1}\">\n");
        patterns.setProperty("lifecycleBeforeScopeEnd", "</scope>\n");
        patterns.setProperty("lifecycleAfterScopeStart", "<scope keyword=\"{0}\" value=\"{1}\">\n");
        patterns.setProperty("lifecycleAfterScopeEnd", "</scope>\n");
        patterns.setProperty("lifecycleOutcomeStart", "<outcome keyword=\"{0}\" value=\"{1}\">\n");
        patterns.setProperty("lifecycleOutcomeEnd", "</outcome>\n");
        patterns.setProperty("lifecycleScope", "<scope>{0} {1}</scope>\n");
        patterns.setProperty("lifecycleOutcome", "<outcome>{0} {1}</outcome>\n");
        patterns.setProperty("lifecycleMetaFilter", "<metaFilter>{0} {1}</metaFilter>\n");
        patterns.setProperty("lifecycleStep", "<step>{0}</step>\n");
        patterns.setProperty("beforeBeforeStorySteps", "<beforeStorySteps>\n");
        patterns.setProperty("afterBeforeStorySteps", "</beforeStorySteps>\n");
        patterns.setProperty("beforeAfterStorySteps", "<afterStorySteps>\n");
        patterns.setProperty("afterAfterStorySteps", "</afterStorySteps>\n");
        patterns.setProperty("beforeScenario", "<scenario keyword=\"{0}\" title=\"{1}\">\n");
        patterns.setProperty("scenarioNotAllowed", "<notAllowed pattern=\"{0}\"/>\n");        
        patterns.setProperty("afterScenario", "</scenario>\n");
        patterns.setProperty("afterScenarioWithFailure", "<failure>{0}</failure>\n</scenario>\n");
        patterns.setProperty("givenStories", "<givenStories keyword=\"{0}\" paths=\"{1}\"/>\n");
        patterns.setProperty("givenStoriesStart", "<givenStories keyword=\"{0}\">\n");
        patterns.setProperty("givenStory", "<givenStory parameters=\"{1}\">{0}</givenStory>\n");
        patterns.setProperty("givenStoriesEnd", "</givenStories>\n");
        patterns.setProperty("successful", "<step outcome=\"successful\">{0}</step>\n");
        patterns.setProperty("ignorable", "<step outcome=\"ignorable\">{0}</step>\n");
        patterns.setProperty("comment", "<step outcome=\"comment\">{0}</step>\n");
        patterns.setProperty("pending", "<step outcome=\"pending\" keyword=\"{1}\">{0}</step>\n");
        patterns.setProperty("notPerformed", "<step outcome=\"notPerformed\" keyword=\"{1}\">{0}</step>\n");
        patterns.setProperty("failed", "<step outcome=\"failed\" keyword=\"{1}\">{0}<failure>{2}</failure></step>\n");
        patterns.setProperty("restarted", "<step outcome=\"restarted\">{0}<reason>{1}</reason></step>\n");
        patterns.setProperty("restartedStory", "<story outcome=\"restartedStory\">{0}<reason>{1}</reason></story>\n");
        patterns.setProperty("outcomesTableStart", "<outcomes>\n");
        patterns.setProperty("outcomesTableHeadStart", "<fields>");
        patterns.setProperty("outcomesTableHeadCell", "<field>{0}</field>");
        patterns.setProperty("outcomesTableHeadEnd", "</fields>\n");
        patterns.setProperty("outcomesTableBodyStart", "");
        patterns.setProperty("outcomesTableRowStart", "<outcome>");
        patterns.setProperty("outcomesTableCell", "<value>{0}</value>");
        patterns.setProperty("outcomesTableRowEnd", "</outcome>\n");
        patterns.setProperty("outcomesTableBodyEnd", "");
        patterns.setProperty("outcomesTableEnd", "</outcomes>\n");
        patterns.setProperty("beforeExamples", "<examples keyword=\"{0}\">\n");
        patterns.setProperty("examplesStep", "<step>{0}</step>\n");
        patterns.setProperty("afterExamples", "</examples>\n");
        patterns.setProperty("examplesTableStart", "<parameters>\n");
        patterns.setProperty("examplesTableHeadStart", "<names>");
        patterns.setProperty("examplesTableHeadCell", "<name>{0}</name>");
        patterns.setProperty("examplesTableHeadEnd", "</names>\n");
        patterns.setProperty("examplesTableBodyStart", "");
        patterns.setProperty("examplesTableRowStart", "<values>");
        patterns.setProperty("examplesTableCell", "<value>{0}</value>");
        patterns.setProperty("examplesTableRowEnd", "</values>\n");
        patterns.setProperty("examplesTableBodyEnd", "");
        patterns.setProperty("examplesTableEnd", "</parameters>\n");
        patterns.setProperty("example", "\n<example keyword=\"{0}\">{1}</example>\n");
        patterns.setProperty("parameterValueStart", "<parameter>");
        patterns.setProperty("parameterValueEnd", "</parameter>");
        patterns.setProperty("parameterValueNewline", "\n");
        patterns.setProperty("numericParameter", "<timing phase=\"{0}\">{1}</timing>\n");
        return patterns;
    }
}
