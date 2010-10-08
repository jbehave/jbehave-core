package org.jbehave.core.reporters;

import static org.jbehave.core.reporters.PrintStreamOutput.Format.XML;

import java.io.PrintStream;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;

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
        super(XML, output, mergeWithDefault(outputPatterns), keywords, reportFailureTrace);
    }

    private static Properties mergeWithDefault(Properties outputPatterns) {
        Properties patterns = defaultHtmlPatterns();
        // override any default pattern
        patterns.putAll(outputPatterns);
        return patterns;
    }

    private static Properties defaultHtmlPatterns() {
        Properties patterns = new Properties();
        patterns.setProperty("metaStart", "<meta>\n");
        patterns.setProperty("metaProperty", "<property prefix=\"{0}\" name=\"{1}\" value=\"{2}\"/>\n");        
        patterns.setProperty("metaEnd", "</meta>\n");
        patterns.setProperty("successful", "<step outcome=\"successful\">{0}</step>\n");
        patterns.setProperty("ignorable", "<step outcome=\"ignorable\">{0}</step>\n");
        patterns.setProperty("pending", "<step outcome=\"pending\" keyword=\"{1}\">{0}</step>\n");
        patterns.setProperty("notPerformed", "<step outcome=\"notPerformed\" keyword=\"{1}\">{0}</step>\n");
        patterns.setProperty("failed", "<step outcome=\"failed\" keyword=\"{1}\">{0}<failure>{2}</failure></step>\n");
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
        patterns.setProperty("beforeStory", "<story path=\"{1}\" title=\"{0}\">\n");
        patterns.setProperty("narrative", "<narrative keyword=\"{0}\">\n  <inOrderTo keyword=\"{1}\">{2}</inOrderTo>\n  <asA keyword=\"{3}\">{4}</asA>\n  <iWantTo keyword=\"{5}\">{6}</iWantTo>\n</narrative>\n");
        patterns.setProperty("afterStory", "</story>\n");
        patterns.setProperty("beforeScenario", "<scenario keyword=\"{0}\" title=\"{1}\">\n");
        patterns.setProperty("afterScenario", "</scenario>\n");
        patterns.setProperty("afterScenarioWithFailure", "<failure>{0}</failure>\n</scenario>\n");
        patterns.setProperty("givenStories", "<givenStories keyword=\"{0}\" paths=\"{1}\"/>\n");
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
        patterns.setProperty("dryRun", "<dryRun>{0}</dryRun>\n");        
        return patterns;
    }

}
