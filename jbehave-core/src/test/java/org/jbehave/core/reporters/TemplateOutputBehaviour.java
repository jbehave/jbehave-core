package org.jbehave.core.reporters;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class TemplateOutputBehaviour extends AbstractOutputBehaviour {

    @Test
    void shouldOutputStoryToHtml() throws IOException {
        // Given
        String name = "template-story.html";
        File file = newFile("target/" + name);
        StoryReporter reporter = new HtmlTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertXml(name, file);
    }

    @Test
    void shouldOutputStoryToJson() throws IOException, SAXException, ParserConfigurationException {
        // Given
        String name = "template-story.json";
        File file = newFile("target/" + name);
        StoryReporter reporter = new JsonTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertJson(name, fileContent(file));
        validateFileOutput(file);
    }

    @Test
    void shouldOutputStoryToXml() throws IOException, SAXException, ParserConfigurationException {
        // Given
        String name = "template-story.xml";
        File file = newFile("target/" + name);
        StoryReporter reporter = new XmlTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertXml(name, file);
        validateFileOutput(file);
    }


    @Test
    void shouldNotGenerateViewWithInexistentTemplates() {
        // Given
        Properties templates = new Properties();
        templates.setProperty("reports", "target/inexistent");
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        // When
        File outputDirectory = new File("target");
        List<String> formats = singletonList("html");
        assertThrows(TemplateableViewGenerator.ViewGenerationFailedForTemplate.class,
                () -> viewGenerator.generateReportsView(outputDirectory, formats, templates));
        // Then ... fail as expected
    }


}
