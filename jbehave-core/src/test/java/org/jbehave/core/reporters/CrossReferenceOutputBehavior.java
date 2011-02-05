package org.jbehave.core.reporters;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CrossReferenceOutputBehavior {

    @Test
    public void handlingOfStoriesAndStepsShouldMakeXmlAndJsonFilesOnTheFileSystem() throws Exception {

        // would put something on the file system, but we're subverting that.
        FilePrintStreamFactory psf = mock(FilePrintStreamFactory.class);


        final List<ByteArrayOutputStream> baoss = new ArrayList<ByteArrayOutputStream>();
        final File zebra = new File("zebra");

        CrossReferenceOutput cro = new CrossReferenceOutput() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baoss.add(baos);
                return new OutputStreamWriter(baos);
            }
        };

        StoryReporterBuilder srb = mock(StoryReporterBuilder.class);
        when(srb.outputDirectory()).thenReturn(zebra);

        // interactions
        PrintStreamOutputBehaviour.narrateAnInterestingStory(cro.createStoryReporter(psf, srb));
        cro.getStepMonitor().stepMatchesPattern("a", true, "[abc]", Object.class.getDeclaredMethods()[0], new Object());

        verifyNoMoreInteractions(psf, srb);

        // generate XML and JSON
        cro.outputToFiles(srb); // fills two ByteArrayOutputStreams above.

        assertEquals("<xref>\n" +
                "  <meta>\n" +
                "    <string>author=Mauro</string>\n" +
                "    <string>author=Mauro\n" +
                "theme=testing</string>\n" +
                "  </meta>\n" +
                "  <stories>\n" +
                "    <org.jbehave.core.reporters.CrossReferenceOutput_-Stori>\n" +
                "      <description>An interesting story</description>\n" +
                "      <narrative>In order to renovate my house\n" +
                "As a customer\n" +
                "I want to get a loan\n" +
                "</narrative>\n" +
                "      <name>/path/to/story</name>\n" +
                "      <path>/path/to/story</path>\n" +
                "      <meta>author=Mauro\n" +
                "theme=testing\n" +
                "</meta>\n" +
                "      <scenarios></scenarios>\n" +
                "    </org.jbehave.core.reporters.CrossReferenceOutput_-Stori>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <StepMatch>\n" +
                "      <storyPath>/path/to/story</storyPath>\n" +
                "      <scenarioTitle>I ask for a loan</scenarioTitle>\n" +
                "      <step>a</step>\n" +
                "      <pattern>[abc]</pattern>\n" +
                "    </StepMatch>\n" +
                "  </stepMatches>\n" +
                "</xref>", baoss.get(0).toString()); // xml

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
                "      'path': '/path/to/story',\n" +
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
                "}}", baoss.get(1).toString().replace('\"', '\'')); // json

    }
}
