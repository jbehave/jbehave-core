package org.jbehave.core.reporters;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TemplateOutputBehaviour extends AbstractOutputBehaviour {

    @Test
    public void shouldOutputStoryToHtml() throws IOException {
        // Given
        String name = "template-story.html";
        File file = newFile("target/" + name);
        StoryReporter reporter = new HtmlTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    public void shouldOutputStoryToJson() throws IOException {
        // Given
        String name = "template-story.json";
        File file = newFile("target/" + name);
        StoryReporter reporter = new JsonTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertFileOutputIsSameAs(file, name);
        //validateFileOutput(file);
    }

    @Test
    public void shouldOutputStoryToXml() throws IOException, SAXException {
        // Given
        String name = "template-story.xml";
        File file = newFile("target/" + name);
        StoryReporter reporter = new XmlTemplateOutput(file, new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertFileOutputIsSameAs(file, name);
        validateFileOutput(file);
    }


    @Test
    public void shouldNotGenerateViewWithInexistentTemplates() {
        // Given
        Properties templates = new Properties();
        templates.setProperty("reports", "target/inexistent");
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        // When
        File outputDirectory = new File("target");
        try {
            viewGenerator.generateReportsView(outputDirectory, asList("html"), templates);
        } catch (Exception e) {
            assertThat(e, is(instanceOf(TemplateableViewGenerator.ViewGenerationFailedForTemplate.class)));
        }
        // Then ... fail as expected
    }


}
