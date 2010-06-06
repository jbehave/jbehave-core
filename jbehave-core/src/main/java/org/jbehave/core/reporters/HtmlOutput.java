package org.jbehave.core.reporters;

import java.io.PrintStream;
import java.util.Properties;

import org.jbehave.core.model.Keywords;

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
        this(output, defaultHtmlPatterns());
    }

    public HtmlOutput(PrintStream output, Properties outputPatterns) {
        super(mergeWithDefault(outputPatterns), Format.HTML);
        usePrintStream(output);
    }
    
    public HtmlOutput(PrintStream output, Properties outputPatterns,
            Keywords keywords, boolean reportErrors) {
        super(output, mergeWithDefault(outputPatterns), Format.HTML, keywords, reportErrors);
    }

    private static Properties mergeWithDefault(Properties outputPatterns) {
        Properties patterns = defaultHtmlPatterns();
        // override any default pattern
        patterns.putAll(outputPatterns);
        return patterns;
    }

    private static Properties defaultHtmlPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("successful", "<div class=\"step successful\">{0}</div>\n");
        patterns.setProperty("ignorable", "<div class=\"step ignorable\">{0}</div>\n");
        patterns.setProperty("pending", "<div class=\"step pending\">{0} <span class=\"keyword pending\">({1})</span></div>\n");
        patterns.setProperty("notPerformed", "<div class=\"step notPerformed\">{0} <span class=\"keyword notPerformed\">({1})</span></div>\n");
        patterns.setProperty("failed", "<div class=\"step failed\">{0} <span class=\"keyword failed\">({1})</span></div>\n");
        patterns.setProperty("beforeStory", "<div class=\"story\">\n<h1>{0}</h1>\n<div class=\"path\">{1}</div>\n");
        patterns.setProperty("narrative", "<div class=\"narrative\"><h2>{0}</h2>\n<div class=\"element inOrderTo\"><span class=\"keyword inOrderTo\">{1}</span> {2}</div>\n<div class=\"element asA\"><span class=\"keyword asA\">{3}</span> {4}</div>\n<div class=\"element iWantTo\"><span class=\"keyword iWantTo\">{5}</span> {6}</div>\n</div>\n");        
        patterns.setProperty("afterStory", "</div>\n");
        patterns.setProperty("beforeScenario", "<div class=\"core\">\n<h2>{0} {1}</h2>\n");
        patterns.setProperty("afterScenario", "</div>\n");
        patterns.setProperty("afterScenarioWithFailure", "<div class=\"core.failure\">{0}</div>\n</div>\n");
        patterns.setProperty("givenStories", "<div class=\"givenStories\">{0} {1}</div>\n");
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
        patterns.setProperty("dryRun", "<div class=\"dryRun\">{0}</div>\n");        
        return patterns;
    }

}
