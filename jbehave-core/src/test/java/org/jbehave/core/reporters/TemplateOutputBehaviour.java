package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.custommonkey.xmlunit.XMLUnit;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TemplateOutputBehaviour extends AbstractOutputBehaviour {

    @Test
    public void shouldOutputStoryToHtml() throws IOException {
        // Given
        File file = new File("target/story-template.html");
        StoryReporter reporter = new HtmlTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        String out = IOUtils.toString(new FileReader(file), true);
        assertThatOutputIs(out, "/story-template.html");
    }

    @Test
    public void shouldOutputStoryToXml() throws IOException, SAXException {
        // Given
        File file = new File("target/story-template.xml");
        StoryReporter reporter = new XmlTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        String out = IOUtils.toString(new FileReader(file), true);

        // will throw SAXException if the xml file is not well-formed
        XMLUnit.buildTestDocument(out);
        assertThatOutputIs(out, "/story-template.xml");
    }

    @Test(expected = TemplateableViewGenerator.ViewGenerationFailedForTemplate.class)
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


}
