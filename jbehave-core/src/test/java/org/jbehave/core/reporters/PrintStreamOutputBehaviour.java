package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.custommonkey.xmlunit.XMLUnit;
import org.jbehave.core.failures.KnownFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.IOUtils;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.reporters.StoryNarrator.IsDateEqual;
import org.jbehave.core.reporters.TemplateableViewGenerator.ViewGenerationFailedForTemplate;
import org.junit.Test;
import org.xml.sax.SAXException;

public class PrintStreamOutputBehaviour {

    @Test
    public void shouldReportEventsToTxtOutput() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new TxtOutput(new PrintStream(out));

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        String expected = "An interesting story & special chars\n"
                + "(/path/to/story)\n"
                + "Meta:\n"
                + "@author Mauro\n"
                + "@theme testing\n"
                + "\n"
                + "DRY RUN\n"
                + "Narrative:\n"
                + "In order to renovate my house\n"
                + "As a customer\n"
                + "I want to get a loan\n"
                + "Scenario: I ask for a loan\n"
                + "GivenStories:\n"
                + "/given/story1 \n"
                + "/given/story2 \n"
                + "\n"
                + "Given I have a balance of $50\n"
                + "!-- A comment\n"
                + "When I request $20\n"
                + "When I ask Liz for a loan of $100\n"
                + "When I ask Liz for a loan of $99\n"
                + "When I write special chars <>&\"\n"
                + "When I write special chars in parameter <>&\"\n"
                + "When I write two parameters ,,, and &&&\n"
                + "Then I should... - try again (hi)\n"
                + "/path/to/story (Restarted Story)\n"
                + "STORY CANCELLED (DURATION 2 s)\n"
                + "Then I should have a balance of $30 (PENDING)\n"
                + "Then I should have $20 (NOT PERFORMED)\n"
                + "Then I don't return loan (FAILED)\n"
                + "(org.jbehave.core.model.OutcomesTable$OutcomesFailed)\n"
                + "|Description|Value|Matcher|Verified|\n"
                + "|I don't return all|100|<50.0>|No|\n"
                + "|A wrong date|01/01/2011|\"02/01/2011\"|No|\n"
                + "\n"
                + "Scenario: Parametrised Scenario\n"
                + "Examples:\n"
                + "Given money <money>\n"
                + "Then I give it to <to>\n"
                + "\n"
                + "|money|to|\n" + "|$30|Mauro|\n"
                + "|$50|Paul|\n" // Examples table
                + "\nExample: {money=$30, to=Mauro}\n"
                + "Given money $30\n"
                + "Then I give it to Mauro\n"
                + "\nExample: {money=$50, to=Paul}\n"
                + "Given money $50\n"
                + "Then I give it to Paul\n"
                + "Then I should have a balance of $30 (PENDING)\n"
                + "\n" // end of examples
                + "\n" // end of scenario
                // pending methods
                + "@When(\"something \\\"$param\\\"\")\n"
                + "@Pending\n"
                + "public void whenSomething() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "\n"
                + "@Then(\"something is <param1>\")\n"
                + "@Pending\n"
                + "public void thenSomethingIsParam1() {\n"
                + "  // PENDING\n"
                + "}\n\n"
                + "\n"; // end of story
        assertThatOutputIs(out, expected);
    }

    @Test
    public void shouldReportEventsToTxtOutputWhenNotAllowedByFilter() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new TxtOutput(new PrintStream(out));

        // When
        StoryNarrator
                .narrateAnInterestingStoryNotAllowedByFilter(reporter, false);

        // Then
        String expected = "An interesting story\n"
                +"(/path/to/story)\n"
                +"Meta:\n"
                +"@author Mauro\n"
                +"@theme testing\n\n"
                +"Scenario: A scenario\n"
                +"-theme testing\n\n\n";
        assertThatOutputIs(out, expected);
    }

    @Test
    public void shouldReportEventsToHtmlOutput() {
        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        StoryReporter reporter = new HtmlOutput(factory.createPrintStream());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        String expected = "<div class=\"story\">\n"
                + "<h1>An interesting story &amp; special chars</h1>\n"
                + "<div class=\"path\">/path/to/story</div>\n"
                + "<div class=\"meta\">\n"
                + "<div class=\"keyword\">Meta:</div>\n"
                + "<div class=\"property\">@author Mauro</div>\n"
                + "<div class=\"property\">@theme testing</div>\n"
                + "</div>\n"
                + "<div class=\"dryRun\">DRY RUN</div>\n"
                + "<div class=\"narrative\"><h2>Narrative:</h2>\n"
                + "<div class=\"element inOrderTo\"><span class=\"keyword inOrderTo\">In order to</span> renovate my house</div>\n"
                + "<div class=\"element asA\"><span class=\"keyword asA\">As a</span> customer</div>\n"
                + "<div class=\"element iWantTo\"><span class=\"keyword iWantTo\">I want to</span> get a loan</div>\n"
                + "</div>\n"
                + "<div class=\"scenario\">\n"
                + "<h2>Scenario: I ask for a loan</h2>\n"
                + "<div class=\"givenStories\">GivenStories:\n"
                + "<div class=\"givenStory\">/given/story1 </div>\n"
                + "<div class=\"givenStory\">/given/story2 </div>\n"
                + "</div>\n"
                + "<div class=\"step successful\">Given I have a balance of $50</div>\n"
                + "<div class=\"step ignorable\">!-- A comment</div>\n"
                + "<div class=\"step successful\">When I request $20</div>\n"
                + "<div class=\"step successful\">When I ask Liz for a loan of $100</div>\n"
                + "<div class=\"step successful\">When I ask Liz for a loan of $<span class=\"step parameter\">99</span></div>\n"
                + "<div class=\"step successful\">When I write special chars &lt;&gt;&amp;&quot;</div>\n"
                + "<div class=\"step successful\">When I write special chars in parameter <span class=\"step parameter\">&lt;&gt;&amp;&quot;</span></div>\n"
                + "<div class=\"step successful\">When I write two parameters <span class=\"step parameter\">,,,</span> and <span class=\"step parameter\">&amp;&amp;&amp;</span></div>\n"
                + "<div class=\"step restarted\">Then I should... - try again <span class=\"message restarted\">hi</span></div>\n"
                + "<div class=\"story restarted\">/path/to/story <span class=\"message restarted\">Restarted Story</span></div>\n"
                + "<div class=\"cancelled\">STORY CANCELLED (DURATION 2 s)</div>\n"
                + "<div class=\"step pending\">Then I should have a balance of $30 <span class=\"keyword pending\">(PENDING)</span></div>\n"
                + "<div class=\"step notPerformed\">Then I should have $20 <span class=\"keyword notPerformed\">(NOT PERFORMED)</span></div>\n"
                + "<div class=\"step failed\">Then I don't return loan <span class=\"keyword failed\">(FAILED)</span><br/><span class=\"message failed\">org.jbehave.core.model.OutcomesTable$OutcomesFailed</span></div>\n"
                + "<div class=\"outcomes\"><table>\n"
                + "<thead>\n"
                + "<tr>\n"
                + "<th>Description</th><th>Value</th><th>Matcher</th><th>Verified</th></tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr class=\"notVerified\">\n"
                + "<td>I don't return all</td><td>100.0</td><td>&lt;50.0&gt;</td><td>No</td></tr>\n"
                + "<tr class=\"notVerified\">\n"
                + "<td>A wrong date</td><td>01/01/2011</td><td>&quot;02/01/2011&quot;</td><td>No</td></tr>\n"
                + "</tbody>\n"
                + "</table></div>\n"
                + "</div>\n"
                + "<div class=\"scenario\">\n"
                + "<h2>Scenario: Parametrised Scenario</h2>\n"
                + "<div class=\"examples\">\n"
                + "<h3>Examples:</h3>\n"
                + "<div class=\"step\">Given money &lt;money&gt;</div>\n"
                + "<div class=\"step\">Then I give it to &lt;to&gt;</div>\n"
                + "<table>\n"
                + "<thead>\n"
                + "<tr>\n"
                + "<th>money</th><th>to</th></tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr>\n"
                + "<td>$30</td><td>Mauro</td></tr>\n"
                + "<tr>\n"
                + "<td>$50</td><td>Paul</td></tr>\n"
                + "</tbody>\n"
                + "</table>\n"
                + "\n"
                + "<h3 class=\"example\">Example: {money=$30, to=Mauro}</h3>\n"
                + "<div class=\"step successful\">Given money $30</div>\n"
                + "<div class=\"step successful\">Then I give it to Mauro</div>\n"
                + "\n"
                + "<h3 class=\"example\">Example: {money=$50, to=Paul}</h3>\n"
                + "<div class=\"step successful\">Given money $50</div>\n"
                + "<div class=\"step successful\">Then I give it to Paul</div>\n"
                + "<div class=\"step pending\">Then I should have a balance of $30 <span class=\"keyword pending\">(PENDING)</span></div>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div><pre class=\"pending\">@When(&quot;something \\&quot;$param\\&quot;&quot;)\n"
                + "@Pending\n"
                + "public void whenSomething() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "</pre></div>\n"
                + "<div><pre class=\"pending\">@Then(&quot;something is &lt;param1&gt;&quot;)\n"
                + "@Pending\n"
                + "public void thenSomethingIsParam1() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "</pre></div>\n"
                + "</div>\n"; // end of story
        assertThatOutputIs(out, expected);
    }

    @Test
    public void shouldReportEventsToHtmlOutputWhenNotAllowedByFilter() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new HtmlOutput(new PrintStream(out));

        // When
        StoryNarrator
                .narrateAnInterestingStoryNotAllowedByFilter(reporter, false);

        // Then
        String expected ="<div class=\"story\">\n"
                + "<h1>An interesting story</h1>\n"
                + "<div class=\"path\">/path/to/story</div>\n"
                + "<div class=\"meta\">\n"
                + "<div class=\"keyword\">Meta:</div>\n"
                + "<div class=\"property\">@author Mauro</div>\n"
                + "<div class=\"property\">@theme testing</div>\n"
                + "</div>\n"
                + "<div class=\"scenario\">\n"
                + "<h2>Scenario: A scenario</h2>\n"
                + "<div class=\"filter\">-theme testing</div>\n"
                + "</div>\n"
                + "</div>\n";
        assertThatOutputIs(out, expected);
    }
    
    @Test
    public void shouldReportEventsToHtmlOutputUsingCustomPatterns() {
        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        Properties patterns = new Properties();
        patterns.setProperty("afterStory", "</div><!-- after story -->\n");
        patterns.setProperty("afterScenario", "</div><!-- after scenario -->\n");
        patterns.setProperty("afterExamples", "</div><!-- after examples -->\n");
        StoryReporter reporter = new HtmlOutput(factory.createPrintStream(), patterns);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        String expected =  "<div class=\"story\">\n"
                + "<h1>An interesting story &amp; special chars</h1>\n"
                + "<div class=\"path\">/path/to/story</div>\n"
                + "<div class=\"meta\">\n"
                + "<div class=\"keyword\">Meta:</div>\n"
                + "<div class=\"property\">@author Mauro</div>\n"
                + "<div class=\"property\">@theme testing</div>\n"
                + "</div>\n"
                + "<div class=\"dryRun\">DRY RUN</div>\n"
                + "<div class=\"narrative\"><h2>Narrative:</h2>\n"
                + "<div class=\"element inOrderTo\"><span class=\"keyword inOrderTo\">In order to</span> renovate my house</div>\n"
                + "<div class=\"element asA\"><span class=\"keyword asA\">As a</span> customer</div>\n"
                + "<div class=\"element iWantTo\"><span class=\"keyword iWantTo\">I want to</span> get a loan</div>\n"
                + "</div>\n"
                + "<div class=\"scenario\">\n"
                + "<h2>Scenario: I ask for a loan</h2>\n"
                + "<div class=\"givenStories\">GivenStories:\n"
                + "<div class=\"givenStory\">/given/story1 </div>\n"
                + "<div class=\"givenStory\">/given/story2 </div>\n"
                + "</div>\n"
                + "<div class=\"step successful\">Given I have a balance of $50</div>\n"
                + "<div class=\"step ignorable\">!-- A comment</div>\n"
                + "<div class=\"step successful\">When I request $20</div>\n"
                + "<div class=\"step successful\">When I ask Liz for a loan of $100</div>\n"
                + "<div class=\"step successful\">When I ask Liz for a loan of $<span class=\"step parameter\">99</span></div>\n"
                + "<div class=\"step successful\">When I write special chars &lt;&gt;&amp;&quot;</div>\n"
                + "<div class=\"step successful\">When I write special chars in parameter <span class=\"step parameter\">&lt;&gt;&amp;&quot;</span></div>\n"
                + "<div class=\"step successful\">When I write two parameters <span class=\"step parameter\">,,,</span> and <span class=\"step parameter\">&amp;&amp;&amp;</span></div>\n"
                + "<div class=\"step restarted\">Then I should... - try again <span class=\"message restarted\">hi</span></div>\n"
                + "<div class=\"story restarted\">/path/to/story <span class=\"message restarted\">Restarted Story</span></div>\n"
                + "<div class=\"cancelled\">STORY CANCELLED (DURATION 2 s)</div>\n"
                + "<div class=\"step pending\">Then I should have a balance of $30 <span class=\"keyword pending\">(PENDING)</span></div>\n"
                + "<div class=\"step notPerformed\">Then I should have $20 <span class=\"keyword notPerformed\">(NOT PERFORMED)</span></div>\n"
                + "<div class=\"step failed\">Then I don't return loan <span class=\"keyword failed\">(FAILED)</span><br/><span class=\"message failed\">org.jbehave.core.model.OutcomesTable$OutcomesFailed</span></div>\n"
                + "<div class=\"outcomes\"><table>\n"
                + "<thead>\n"
                + "<tr>\n"
                + "<th>Description</th><th>Value</th><th>Matcher</th><th>Verified</th></tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr class=\"notVerified\">\n"
                + "<td>I don't return all</td><td>100.0</td><td>&lt;50.0&gt;</td><td>No</td></tr>\n"
                + "<tr class=\"notVerified\">\n"
                + "<td>A wrong date</td><td>01/01/2011</td><td>&quot;02/01/2011&quot;</td><td>No</td></tr>\n"
                + "</tbody>\n"
                + "</table></div>\n"
                + "</div><!-- after scenario -->\n"
                + "<div class=\"scenario\">\n"
                + "<h2>Scenario: Parametrised Scenario</h2>\n"
                + "<div class=\"examples\">\n"
                + "<h3>Examples:</h3>\n"
                + "<div class=\"step\">Given money &lt;money&gt;</div>\n"
                + "<div class=\"step\">Then I give it to &lt;to&gt;</div>\n"
                + "<table>\n"
                + "<thead>\n"
                + "<tr>\n"
                + "<th>money</th><th>to</th></tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr>\n"
                + "<td>$30</td><td>Mauro</td></tr>\n"
                + "<tr>\n"
                + "<td>$50</td><td>Paul</td></tr>\n"
                + "</tbody>\n"
                + "</table>\n"
                + "\n"
                + "<h3 class=\"example\">Example: {money=$30, to=Mauro}</h3>\n"
                + "<div class=\"step successful\">Given money $30</div>\n"
                + "<div class=\"step successful\">Then I give it to Mauro</div>\n"
                + "\n"
                + "<h3 class=\"example\">Example: {money=$50, to=Paul}</h3>\n"
                + "<div class=\"step successful\">Given money $50</div>\n"
                + "<div class=\"step successful\">Then I give it to Paul</div>\n"
                + "<div class=\"step pending\">Then I should have a balance of $30 <span class=\"keyword pending\">(PENDING)</span></div>\n"
                + "</div><!-- after examples -->\n"
                + "</div><!-- after scenario -->\n"
                // pending methods
                + "<div><pre class=\"pending\">@When(&quot;something \\&quot;$param\\&quot;&quot;)\n"
                + "@Pending\n"
                + "public void whenSomething() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "</pre></div>\n"
                + "<div><pre class=\"pending\">@Then(&quot;something is &lt;param1&gt;&quot;)\n"
                + "@Pending\n"
                + "public void thenSomethingIsParam1() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "</pre></div>\n"
                + "</div><!-- after story -->\n";
        assertThatOutputIs(out, expected);
    }

    @Test
    public void shouldReportEventsToXmlOutput() throws SAXException, IOException {
        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        StoryReporter reporter = new XmlOutput(factory.createPrintStream());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        String expected = "<story path=\"/path/to/story\" title=\"An interesting story &amp; special chars\">\n"
                + "<meta>\n"
                + "<property keyword=\"@\" name=\"author\" value=\"Mauro\"/>\n"
                + "<property keyword=\"@\" name=\"theme\" value=\"testing\"/>\n"
                + "</meta>\n"
                + "<dryRun>DRY RUN</dryRun>\n"
                + "<narrative keyword=\"Narrative:\">\n"
                + "  <inOrderTo keyword=\"In order to\">renovate my house</inOrderTo>\n"
                + "  <asA keyword=\"As a\">customer</asA>\n"
                + "  <iWantTo keyword=\"I want to\">get a loan</iWantTo>\n"
                + "</narrative>\n"
                + "<scenario keyword=\"Scenario:\" title=\"I ask for a loan\">\n"
                + "<givenStories keyword=\"GivenStories:\">\n"
                + "<givenStory parameters=\"\">/given/story1</givenStory>\n"
                + "<givenStory parameters=\"\">/given/story2</givenStory>\n"
                + "</givenStories>\n"
                + "<step outcome=\"successful\">Given I have a balance of $50</step>\n"
                + "<step outcome=\"ignorable\">!-- A comment</step>\n"
                + "<step outcome=\"successful\">When I request $20</step>\n"
                + "<step outcome=\"successful\">When I ask Liz for a loan of $100</step>\n"
                + "<step outcome=\"successful\">When I ask Liz for a loan of $<parameter>99</parameter></step>\n"
                + "<step outcome=\"successful\">When I write special chars &lt;&gt;&amp;&quot;</step>\n"
                + "<step outcome=\"successful\">When I write special chars in parameter <parameter>&lt;&gt;&amp;&quot;</parameter></step>\n"
                + "<step outcome=\"successful\">When I write two parameters <parameter>,,,</parameter> and <parameter>&amp;&amp;&amp;</parameter></step>\n"
                + "<step outcome=\"restarted\">Then I should... - try again<reason>hi</reason></step>\n"
                + "<story outcome=\"restartedStory\">/path/to/story<reason>Restarted Story</reason></story>\n"
                + "<cancelled keyword=\"STORY CANCELLED\" durationKeyword=\"DURATION\" durationInSecs=\"2\"/>\n"
                + "<step outcome=\"pending\" keyword=\"PENDING\">Then I should have a balance of $30</step>\n"
                + "<step outcome=\"notPerformed\" keyword=\"NOT PERFORMED\">Then I should have $20</step>\n"
                + "<step outcome=\"failed\" keyword=\"FAILED\">Then I don&apos;t return loan<failure>org.jbehave.core.model.OutcomesTable$OutcomesFailed</failure></step>\n"
                + "<outcomes>\n"
                + "<fields><field>Description</field><field>Value</field><field>Matcher</field><field>Verified</field></fields>\n"
                + "<outcome><value>I don&apos;t return all</value><value>100.0</value><value>&lt;50.0&gt;</value><value>No</value></outcome>\n"
                + "<outcome><value>A wrong date</value><value>01/01/2011</value><value>&quot;02/01/2011&quot;</value><value>No</value></outcome>\n"
                + "</outcomes>\n"
                + "</scenario>\n"
                + "<scenario keyword=\"Scenario:\" title=\"Parametrised Scenario\">\n"
                + "<examples keyword=\"Examples:\">\n"
                + "<step>Given money &lt;money&gt;</step>\n"
                + "<step>Then I give it to &lt;to&gt;</step>\n"
                + "<parameters>\n"
                + "<names><name>money</name><name>to</name></names>\n"
                + "<values><value>$30</value><value>Mauro</value></values>\n"
                + "<values><value>$50</value><value>Paul</value></values>\n"
                + "</parameters>\n"
                + "\n"
                + "<example keyword=\"Example:\">{money=$30, to=Mauro}</example>\n"
                + "<step outcome=\"successful\">Given money $30</step>\n"
                + "<step outcome=\"successful\">Then I give it to Mauro</step>\n"
                + "\n"
                + "<example keyword=\"Example:\">{money=$50, to=Paul}</example>\n"
                + "<step outcome=\"successful\">Given money $50</step>\n"
                + "<step outcome=\"successful\">Then I give it to Paul</step>\n"
                + "<step outcome=\"pending\" keyword=\"PENDING\">Then I should have a balance of $30</step>\n"
                + "</examples>\n"
                + "</scenario>\n"
                // pending methods
                + "<pendingMethod>@When(&quot;something \\&quot;$param\\&quot;&quot;)\n"
                + "@Pending\n"
                + "public void whenSomething() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "</pendingMethod>\n"
                + "<pendingMethod>@Then(&quot;something is &lt;param1&gt;&quot;)\n"
                + "@Pending\n"
                + "public void thenSomethingIsParam1() {\n"
                + "  // PENDING\n"
                + "}\n"
                + "</pendingMethod>\n"
                + "</story>\n";
        String xmlDocument=out.toString();
        XMLUnit.buildTestDocument(xmlDocument);
        assertEquals(expected, xmlDocument);
    }

    @Test
    public void shouldNotSuppressStackTraceForNotKnownFailure() {

        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        TxtOutput reporter = new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);


        reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new NullPointerException()));
        reporter.afterScenario();

        assertThatOutputStartsWith(out, "Then I should have a balance of $30 (FAILED)\n" +
                "(java.lang.NullPointerException)\n" +
                "\n" +
                "java.lang.NullPointerException\n" +
                "\tat "); // there is a whole stack trace but we're skipping that for the sake of an assertion

    }

    @Test
    public void shouldSuppressStackTraceForKnownFailure() {

        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        TxtOutput reporter = new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);


        reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new MyKnownFailure()));
        reporter.afterScenario();

        assertThatOutputIs(out, "Then I should have a balance of $30 (FAILED)\n" +
                "(org.jbehave.core.reporters.PrintStreamOutputBehaviour$MyKnownFailure)\n\n" +
                "");

    }

    @Test
    public void shouldReportEventsToXmlOutputWhenNotAllowedByFilter() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new XmlOutput(new PrintStream(out));

        // When
        StoryNarrator
                .narrateAnInterestingStoryNotAllowedByFilter(reporter, false);

        // Then
        String expected = "<story path=\"/path/to/story\" title=\"An interesting story\">\n"
                + "<meta>\n"
                + "<property keyword=\"@\" name=\"author\" value=\"Mauro\"/>\n"
                + "<property keyword=\"@\" name=\"theme\" value=\"testing\"/>\n"
                + "</meta>\n"
                + "<scenario keyword=\"Scenario:\" title=\"A scenario\">\n"
                + "<filter>-theme testing</filter>\n"
                + "</scenario>\n"
                + "</story>\n";
        assertThatOutputIs(out, expected);
    }

    private void assertThatOutputIs(OutputStream out, String expected) {
        assertEquals(expected, dos2unix(out.toString()));
    }

    private void assertThatOutputStartsWith(OutputStream out, String expected) {
        assertTrue(dos2unix(out.toString()).startsWith(expected));
    }

    private String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

    @Test
    public void shouldReportFailureTraceWhenToldToDoSo() {
        // Given
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new RuntimeException("Leave my money alone!"));
        OutputStream stackTrace = new ByteArrayOutputStream();
        exception.getCause().printStackTrace(new PrintStream(stackTrace));
        OutputStream out = new ByteArrayOutputStream();
        TxtOutput reporter = new TxtOutput(new PrintStream(out), new Properties(),
                new LocalizedKeywords(), true);

        // When
        reporter.beforeScenario("A title");
        reporter.successful("Given I have a balance of $50");
        reporter.successful("When I request $20");
        reporter.failed("When I ask Liz for a loan of $100", exception);
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        reporter.afterScenario();

        // Then
        String expected = "Scenario: A title\n" 
        		+ "Given I have a balance of $50\n" 
        		+ "When I request $20\n"
                + "When I ask Liz for a loan of $100 (FAILED)\n"
                + "(java.lang.RuntimeException: Leave my money alone!)\n"
                + "Then I should have a balance of $30 (PENDING)\n"
                + "Then I should have $20 (NOT PERFORMED)\n" 
                + "\n";
        String actual = dos2unix(out.toString());
        assertThat(actual, containsString(expected));
        assertThat(actual, containsString("at org.jbehave.core.reporters.PrintStreamOutputBehaviour.shouldReportFailureTraceWhenToldToDoSo("));


        // Given
        out = new ByteArrayOutputStream();
        reporter = new TxtOutput(new PrintStream(out));

        // When
        reporter.beforeScenario("A title");
        reporter.successful("Given I have a balance of $50");
        reporter.successful("When I request $20");
        reporter.failed("When I ask Liz for a loan of $100", exception);
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        reporter.afterScenario();

        // Then
        assertThat(out.toString().contains(stackTrace.toString()), is(false));
    }

    @Test
    public void shouldReportEventsToTxtOutputWithCustomPatterns() {
        // Given
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new RuntimeException("Leave my money alone!"));
        OutputStream out = new ByteArrayOutputStream();
        Properties patterns = new Properties();
        patterns.setProperty("pending", "{0} - {1} - need to implement me\n");
        patterns.setProperty("failed", "{0} <<< {1}\n");
        patterns.setProperty("notPerformed", "{0} : {1} (because of previous pending)\n");
        StoryReporter reporter = new TxtOutput(new PrintStream(out), patterns, new LocalizedKeywords(),
                true);

        // When
        reporter.successful("Given I have a balance of $50");
        reporter.successful("When I request $20");
        reporter.failed("When I ask Liz for a loan of $100", exception);
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");

        // Then
        String expected = "Given I have a balance of $50\n" + "When I request $20\n"
                + "When I ask Liz for a loan of $100 <<< FAILED\n"
                + "Then I should have a balance of $30 - PENDING - need to implement me\n"
                + "Then I should have $20 : NOT PERFORMED (because of previous pending)\n";

        assertThatOutputIs(out, expected);

    }
    
    @Test
    public void shouldReportEventsToIdeOnlyConsoleOutput() {
        // When
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(), false);
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(new LocalizedKeywords()), false);
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(new Properties(), new LocalizedKeywords(), true), false);
    }

    @Test
    public void shouldReportEventsToPrintStreamInItalian() {
        // Given
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new RuntimeException("Lasciate in pace i miei soldi!"));
        OutputStream out = new ByteArrayOutputStream();
        LocalizedKeywords keywords = new LocalizedKeywords(Locale.ITALIAN);
        StoryReporter reporter = new TxtOutput(new PrintStream(out), new Properties(), keywords,
                true);

        // When
        reporter.successful("Dato che ho un saldo di $50");
        reporter.successful("Quando richiedo $20");
        reporter.failed("Quando chiedo a Liz un prestito di $100", exception);
        reporter.pending("Allora dovrei avere un saldo di $30");
        reporter.notPerformed("Allora dovrei avere $20");

        // Then
        String expected = "Dato che ho un saldo di $50\n" 
        		+ "Quando richiedo $20\n"
                + "Quando chiedo a Liz un prestito di $100 (FALLITO)\n"
                + "(java.lang.RuntimeException: Lasciate in pace i miei soldi!)\n"
                + "Allora dovrei avere un saldo di $30 (IN SOSPESO)\n"
                + "Allora dovrei avere $20 (NON ESEGUITO)\n";

        assertThatOutputIs(out, expected);

    }

    @Test
    public void shouldCreateAndWriteToFilePrintStreamForStoryLocation() throws IOException {

        // Given
        String storyPath = storyPath(MyStory.class);
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(this.getClass()), storyPath));
        File file = factory.outputFile();
        file.delete();
        assertThat(file.exists(), is(false));
        
        // When
        PrintStream printStream = factory.createPrintStream();
        file = factory.getOutputFile();
        printStream.print("Hello World");

        // Then
        assertThat(file.exists(), is(true));
        assertThat(IOUtils.toString(new FileReader(file), true), equalTo("Hello World"));
    }

    @Test
    public void shouldReportEventsToFilePrintStreamsAndGenerateView() throws IOException {
        final String storyPath = storyPath(MyStory.class);
        File outputDirectory = new File("target/output");
        StoryReporter reporter = new StoryReporterBuilder().withRelativeDirectory(outputDirectory.getName())
        		.withFormats(HTML, TXT)
                .build(storyPath);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        Properties viewProperties = new Properties();
        viewGenerator.generateReportsView(outputDirectory, asList("html", "txt"), viewProperties);

        // Then
        ensureFileExists(new File(outputDirectory, "view/index.html"));
        ensureFileExists(new File(outputDirectory, "view/org.jbehave.core.reporters.my_story.txt.html"));
    }

    @Test
    public void shouldReportEventsToFilePrintStreamsAndGenerateViewWithoutDecoratingNonHtml() throws IOException {
        final String storyPath = storyPath(MyStory.class);
        File outputDirectory = new File("target/output");
        StoryReporter reporter = new StoryReporterBuilder().withRelativeDirectory(outputDirectory.getName())
                .withFormats(HTML, TXT)
                .build(storyPath);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        ((ConcurrentStoryReporter) reporter).invokeDelayed();
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        Properties viewProperties = new Properties();
        viewProperties.setProperty("decorateNonHtml", "false");
        viewGenerator.generateReportsView(outputDirectory, asList("html", "txt"), viewProperties);

        // Then
        ensureFileExists(new File(outputDirectory, "view/index.html"));
        ensureFileExists(new File(outputDirectory, "view/org.jbehave.core.reporters.my_story.txt"));
    }

    
    @Test
    public void shouldBuildPrintStreamReportersAndOverrideDefaultForAGivenFormat() throws IOException {
        final String storyPath = storyPath(MyStory.class);
        final FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(this.getClass()), storyPath));
        StoryReporter reporter = new StoryReporterBuilder() {
            @Override
            public StoryReporter reporterFor(String storyPath, org.jbehave.core.reporters.Format format) {
                if (format == org.jbehave.core.reporters.Format.TXT) {
                    factory.useConfiguration(new FilePrintStreamFactory.FileConfiguration("text"));
                    return new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);
                } else {
                    return super.reporterFor(storyPath, format);
                }
            }
        }.withFormats(TXT).build(storyPath);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        ((ConcurrentStoryReporter) reporter).invokeDelayed();


        // Then
        File outputFile = factory.getOutputFile();
        ensureFileExists(outputFile);
    }

    private void ensureFileExists(File file) throws IOException, FileNotFoundException {
        assertThat(file.exists(), is(true));
        assertThat(IOUtils.toString(new FileReader(file), true).length(), greaterThan(0));
    }

    @Test(expected = ViewGenerationFailedForTemplate.class)
    public void shouldFailGeneratingViewWithInexistentTemplates() throws IOException {
        // Given
        Properties templates = new Properties();
        templates.setProperty("reports", "target/inexistent");
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        // When
        File outputDirectory = new File("target");
        viewGenerator.generateReportsView(outputDirectory, asList("html"), templates);
        // Then ... fail as expected
    }

    private String storyPath(Class<MyStory> storyClass) {
        StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
        return resolver.resolve(storyClass);
    }

    @Test
    public void shouldUseDateConversionPattern() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new TxtOutput(new PrintStream(out));

        // When
        OutcomesTable outcomesTable = new OutcomesTable(new LocalizedKeywords(), "dd/MM/yyyy");
        Date actualDate = StoryNarrator.dateFor("01/01/2011");
        Date expectedDate = StoryNarrator.dateFor("02/01/2011");
        outcomesTable.addOutcome("A wrong date", actualDate, new IsDateEqual(expectedDate, outcomesTable.getDateFormat()));
        try {
            outcomesTable.verify();
        } catch (UUIDExceptionWrapper e) {
            reporter.failedOutcomes("some step", ((OutcomesFailed) e.getCause()).outcomesTable());
        }

        // Then
        String expected = "some step (FAILED)\n"
                + "(org.jbehave.core.model.OutcomesTable$OutcomesFailed)\n" 
                + "|Description|Value|Matcher|Verified|\n"
                + "|A wrong date|01/01/2011|\"02/01/2011\"|No|\n";
        assertThatOutputIs(out, expected);
    }

    @SuppressWarnings("serial")
    private static class MyKnownFailure extends KnownFailure {
    }

    private abstract class MyStory extends JUnitStory {

    }
}
