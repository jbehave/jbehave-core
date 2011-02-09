package org.jbehave.core.reporters;

import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class CrossReferenceBehaviour {

    @Test
    public void shouldProduceXmlAndJsonOutputsOfStoriesAndSteps() throws Exception {

        // Given
        FilePrintStreamFactory factory = mock(FilePrintStreamFactory.class);

        final List<ByteArrayOutputStream> output = new ArrayList<ByteArrayOutputStream>();
        final File zebra = new File("zebra");

        CrossReference crossReference = new CrossReference() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                output.add(baos);
                return new OutputStreamWriter(baos);
            }
        };

        StoryReporterBuilder builder = mock(StoryReporterBuilder.class);
        when(builder.outputDirectory()).thenReturn(zebra);
        FilePathResolver pathResolver = new ResolveToPackagedName();
        when(builder.pathResolver()).thenReturn(pathResolver);

        // When
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder), true);
        crossReference.getStepMonitor().stepMatchesPattern("a", true, "[abc]", Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON        
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder);

        System.out.println("AAA{" + output.get(0).toString() + "}AAA");

        // Then
        assertEquals("<xref>\n" +
                "  <meta>\n" +
                "    <string>theme=testing</string>\n" +
                "    <string>author=Mauro</string>\n" +
                "  </meta>\n" +
                "  <stories>\n" +
                "    <story>\n" +
                "      <description>An interesting story</description>\n" +
                "      <narrative>In order to renovate my house\n" +
                "As a customer\n" +
                "I want to get a loan\n" +
                "</narrative>\n" +
                "      <name>/path/to/story</name>\n" +
                "      <path>/path/to/story</path>\n" +
                "      <html>path.to.html</html>\n" +
                "      <meta>author=Mauro\n" +
                "theme=testing\n" +
                "</meta>\n" +
                "      <scenarios></scenarios>\n" +
                "      <passed>false</passed>\n" +
                "    </story>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <entry>\n" +
                "      <string>[abc]</string>\n" +
                "      <list>\n" +
                "        <stepMatch>\n" +
                "          <storyPath>/path/to/story</storyPath>\n" +
                "          <scenarioTitle>I ask for a loan</scenarioTitle>\n" +
                "          <step>a</step>\n" +
                "        </stepMatch>\n" +
                "      </list>\n" +
                "    </entry>\n" +
                "  </stepMatches>\n" +
                "</xref>", output.get(0).toString()); // xml

        assertEquals("{'xref': {\n" +
                "  'meta': [\n" +
                "    'theme=testing',\n" +
                "    'author=Mauro'\n" +
                "  ],\n" +
                "  'stories': [\n" +
                "    {\n" +
                "      'description': 'An interesting story',\n" +
                "      'narrative': 'In order to renovate my house\\u000aAs a customer\\u000aI want to get a loan\\u000a',\n" +
                "      'name': '/path/to/story',\n" +
                "      'path': '/path/to/story',\n" +
                "      'html': 'path.to.html',\n" +
                "      'meta': 'author=Mauro\\u000atheme=testing\\u000a',\n" +
                "      'scenarios': '',\n" +
                "      'passed': false\n" +
                "    }\n" +
                "  ],\n" +
                "  'stepMatches': [\n" +
                "    [\n" +
                "      '[abc]',\n" +
                "      [\n" +
                "        {\n" +
                "          'storyPath': '/path/to/story',\n" +
                "          'scenarioTitle': 'I ask for a loan',\n" +
                "          'step': 'a'\n" +
                "        }\n" +
                "      ]\n" +
                "    ]\n" +
                "  ]\n" +
                "}}", output.get(1).toString().replace('\"', '\'')); // json

    }
}
