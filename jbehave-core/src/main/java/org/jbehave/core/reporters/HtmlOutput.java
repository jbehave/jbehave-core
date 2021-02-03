package org.jbehave.core.reporters;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;

import java.io.PrintStream;
import java.util.Properties;

import static org.jbehave.core.reporters.PrintStreamOutput.Format.HTML;

/**
 * <p>
 * Story reporter that outputs to a PrintStream, as HTML. It extends
 * {@link PrintStreamOutput}, providing HTML-based default output
 * patterns, which can be overridden via the {@link
 * HtmlOutput (PrintStream,Properties)} constructor.
 * </p>
 * 
 * @author Mirko FriedenHagen
 * @author Mauro Talevi
 */
public class HtmlOutput extends PrintStreamOutput {

    public HtmlOutput(PrintStream output) {
        this(output, new Properties());
    }

    public HtmlOutput(PrintStream output, Properties outputPatterns) {
        this(output, outputPatterns, new LocalizedKeywords());
    }
    
    public HtmlOutput(PrintStream output, Keywords keywords) {
        this(output, new Properties(), keywords);
    }

    public HtmlOutput(PrintStream output, Properties outputPatterns, Keywords keywords) {
        this(output, outputPatterns, keywords, false);
    }

    public HtmlOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportFailureTrace) {
        this(output, outputPatterns, keywords, reportFailureTrace, false);
    }

    public HtmlOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportFailureTrace, boolean compressFailureTrace) {
        super(HTML, output, defaultHtmlPatterns(), outputPatterns, keywords, reportFailureTrace, compressFailureTrace);
    }

    private static Properties defaultHtmlPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("dryRun", "<div class=\"dryRun\">{0}</div>\n");        
        patterns.setProperty("beforeStory", "<div class=\"story\">\n<h1>{0}</h1>\n<div class=\"path\">{1}</div>\n");
        patterns.setProperty("storyCancelled", "<div class=\"cancelled\">{0} ({1} {2} s)</div>\n");
        patterns.setProperty("afterStory", "</div>\n");
        patterns.setProperty("pendingMethod", "<div><pre class=\"pending\">{0}</pre></div>\n");        
        patterns.setProperty("metaStart", "<div class=\"meta\">\n<div class=\"keyword\">{0}</div>\n");
        patterns.setProperty("metaProperty", "<div class=\"property\">{0}{1} {2}</div>\n");
        patterns.setProperty("metaEnd", "</div>\n");
        patterns.setProperty("filter", "<div class=\"filter\">{0}</div>\n");        
        patterns.setProperty("narrative", "<div class=\"narrative\"><h2>{0}</h2>\n<div class=\"element inOrderTo\"><span class=\"keyword inOrderTo\">{1}</span> {2}</div>\n<div class=\"element asA\"><span class=\"keyword asA\">{3}</span> {4}</div>\n<div class=\"element iWantTo\"><span class=\"keyword iWantTo\">{5}</span> {6}</div>\n</div>\n");        
        patterns.setProperty("lifecycleStart", "<div class=\"lifecycle\"><h2>{0}</h2>\n");
        patterns.setProperty("lifecycleEnd", "</div>\n");
        patterns.setProperty("lifecycleBeforeStart", "<div class=\"before\"><h3>{0}</h3>\n");
        patterns.setProperty("lifecycleBeforeEnd", "</div>\n");
        patterns.setProperty("lifecycleAfterStart", "<div class=\"after\"><h3>{0}</h3>\n");
        patterns.setProperty("lifecycleAfterEnd", "</div>\n");
        patterns.setProperty("lifecycleBeforeScopeStart", "<div class=\"scope\"><h3>{0} {1}</h3>\n");
        patterns.setProperty("lifecycleBeforeScopeEnd", "</div>\n");
        patterns.setProperty("lifecycleAfterScopeStart", "<div class=\"scope\"><h3>{0} {1}</h3>\n");
        patterns.setProperty("lifecycleAfterScopeEnd", "</div>\n");
        patterns.setProperty("lifecycleOutcomeStart", "<div class=\"outcome\"><h3>{0} {1}</h3>\n");
        patterns.setProperty("lifecycleOutcomeEnd", "</div>\n");
        patterns.setProperty("lifecycleMetaFilter", "<div class=\"metaFilter step\">{0} {1}</div>\n");
        patterns.setProperty("lifecycleStep", "<div class=\"step\">{0}</div>\n");
        patterns.setProperty("beforeBeforeStorySteps", "<div class=\"beforeStorySteps\">\n");
        patterns.setProperty("afterBeforeStorySteps", "</div>\n");
        patterns.setProperty("beforeAfterStorySteps", "<div class=\"afterStorySteps\">\n");
        patterns.setProperty("afterAfterStorySteps", "</div>\n");
        patterns.setProperty("beforeScenario", "<div class=\"scenario\">\n<h2>{0} {1}</h2>\n");
        patterns.setProperty("afterScenario", "</div>\n");
        patterns.setProperty("afterScenarioWithFailure", "<pre class=\"failure\">{0}</pre>\n</div>\n");
        patterns.setProperty("givenStories", "<div class=\"givenStories\">{0} {1}</div>\n");
        patterns.setProperty("givenStoriesStart", "<div class=\"givenStories\">{0}\n");
        patterns.setProperty("givenStory", "<div class=\"givenStory\">{0} {1}</div>\n");
        patterns.setProperty("givenStoriesEnd", "</div>\n");
        patterns.setProperty("successful", "<div class=\"step successful\">{0}</div>\n");
        patterns.setProperty("ignorable", "<div class=\"step ignorable\">{0}</div>\n");
        patterns.setProperty("comment", "<div class=\"comment\">{0}</div>\n");
        patterns.setProperty("pending", "<div class=\"step pending\">{0} <span class=\"keyword pending\">({1})</span></div>\n");
        patterns.setProperty("notPerformed", "<div class=\"step notPerformed\">{0} <span class=\"keyword notPerformed\">({1})</span></div>\n");
        patterns.setProperty("failed", "<div class=\"step failed\">{0} <span class=\"keyword failed\">({1})</span><br/><span class=\"message failed\">{2}</span></div>\n");
        patterns.setProperty("restarted", "<div class=\"step restarted\">{0} <span class=\"message restarted\">{1}</span></div>\n");
        patterns.setProperty("restartedStory", "<div class=\"story restarted\">{0} <span class=\"message restarted\">{1}</span></div>\n");
        patterns.setProperty("outcomesTableStart", "<div class=\"outcomes\"><table>\n");
        patterns.setProperty("outcomesTableHeadStart", "<thead>\n<tr>\n");
        patterns.setProperty("outcomesTableHeadCell", "<th>{0}</th>");
        patterns.setProperty("outcomesTableHeadEnd", "</tr>\n</thead>\n");
        patterns.setProperty("outcomesTableBodyStart", "<tbody>\n");
        patterns.setProperty("outcomesTableRowStart", "<tr class=\"{0}\">\n");
        patterns.setProperty("outcomesTableCell", "<td>{0}</td>");
        patterns.setProperty("outcomesTableRowEnd", "</tr>\n");
        patterns.setProperty("outcomesTableBodyEnd", "</tbody>\n");
        patterns.setProperty("outcomesTableEnd", "</table></div>\n");
        patterns.setProperty("beforeExamples", "<div class=\"examples\">\n<h3>{0}</h3>\n");
        patterns.setProperty("examplesStep", "<div class=\"step\">{0}</div>\n");
        patterns.setProperty("afterExamples", "</div>\n");
        patterns.setProperty("examplesTableStart", "<table>\n");
        patterns.setProperty("examplesTableHeadStart", "<thead>\n<tr>\n");
        patterns.setProperty("examplesTableHeadCell", "<th>{0}</th>");
        patterns.setProperty("examplesTableHeadEnd", "</tr>\n</thead>\n");
        patterns.setProperty("examplesTableBodyStart", "<tbody>\n");
        patterns.setProperty("examplesTableRowStart", "<tr>\n");
        patterns.setProperty("examplesTableCell", "<td>{0}</td>");
        patterns.setProperty("examplesTableRowEnd", "</tr>\n");
        patterns.setProperty("examplesTableBodyEnd", "</tbody>\n");
        patterns.setProperty("examplesTableEnd", "</table>\n");
        patterns.setProperty("example", "\n<h3 class=\"example\">{0} {1}</h3>\n");
        patterns.setProperty("parameterValueStart", "<span class=\"step parameter\">");
        patterns.setProperty("parameterValueEnd", "</span>");
        patterns.setProperty("parameterValueNewline", "<br/>");
        patterns.setProperty("numericParameter", "<h3>{0} {1}</h3>\n");
        return patterns;
    }
}
