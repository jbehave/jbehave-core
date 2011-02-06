package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder));
        crossReference.getStepMonitor().stepMatchesPattern("a", true, "[abc]", Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON        
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder); 

        // Then
        assertEquals("<xref>\n" +
                "  <meta>\n" +
                "    <string>author=Mauro</string>\n" +
                "    <string>author=Mauro\n" +
                "theme=testing</string>\n" +
                "  </meta>\n" +
                "  <stories>\n" +
                "    <story>\n" +
                "      <description>An interesting story</description>\n" +
                "      <narrative>In order to renovate my house\n" +
                "As a customer\n" +
                "I want to get a loan\n" +
                "</narrative>\n" +
                "      <name>/path/to/story</name>\n" +
                "      <path>path.to.html</path>\n" +
                "      <meta>author=Mauro\n" +
                "theme=testing\n" +
                "</meta>\n" +
                "      <scenarios></scenarios>\n" +
                "    </story>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <stepMatch>\n" +
                "      <storyPath>/path/to/story</storyPath>\n" +
                "      <scenarioTitle>I ask for a loan</scenarioTitle>\n" +
                "      <step>a</step>\n" +
                "      <pattern>[abc]</pattern>\n" +
                "    </stepMatch>\n" +
                "  </stepMatches>\n" +
                "</xref>", output.get(0).toString()); // xml

        assertEquals("{'xref': {\n" +
                "  'meta': [\n" +
                "    'author=Mauro',\n" +
                "    'author=Mauro\\u000atheme=testing'\n" +
                "  ],\n" +
                "  'stories': [\n" +
                "    {\n" +
                "      'description': 'An interesting story',\n" +
                "      'narrative': 'In order to renovate my house\\u000aAs a customer\\u000aI want to get a loan\\u000a',\n" +
                "      'name': '/path/to/story',\n" +
                "      'path': 'path.to.html',\n" +
                "      'meta': 'author=Mauro\\u000atheme=testing\\u000a',\n" +
                "      'scenarios': ''\n" +
                "    }\n" +
                "  ],\n" +
                "  'stepMatches': [\n" +
                "    {\n" +
                "      'storyPath': '/path/to/story',\n" +
                "      'scenarioTitle': 'I ask for a loan',\n" +
                "      'step': 'a',\n" +
                "      'pattern': '[abc]'\n" +
                "    }\n" +
                "  ]\n" +
                "}}", output.get(1).toString().replace('\"', '\'')); // json

    }
}
