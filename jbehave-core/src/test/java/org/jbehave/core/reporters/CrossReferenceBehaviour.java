package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.model.StepPattern;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

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
        final File zebra = new File("target/zebra");

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
        crossReference.getStepMonitor().stepMatchesPattern("a", true, new StepPattern("(def)", "[abc]"), Object.class.getDeclaredMethods()[0], new Object());

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
                "    <stepMatch>\n" +
                "      <annotatedPattern>(def)</annotatedPattern>\n" +
                "      <resolvedPattern>[abc]</resolvedPattern>\n" +
                "      <usages>\n" +
                "        <use>\n" +
                "          <story>/path/to/story</story>\n" +
                "          <scenario>I ask for a loan</scenario>\n" +
                "          <step>a</step>\n" +
                "        </use>\n" +
                "      </usages>\n" +
                "    </stepMatch>\n" +
                "  </stepMatches>\n" +
                "</xref>", output.get(0).toString()); // xml


        System.out.println("BBB{" + output.get(1).toString() + "}BBB");

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
                "    {\n" +
                "      'annotatedPattern': '(def)',\n" +
                "      'resolvedPattern': '[abc]',\n" +
                "      'usages': [\n" +
                "        {\n" +
                "          'story': '/path/to/story',\n" +
                "          'scenario': 'I ask for a loan',\n" +
                "          'step': 'a'\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}}", output.get(1).toString().replace('\"', '\'')); // json

    }

    @Test
    public void shouldAllowOverridingOfObjectsCreated() throws Exception {

        // Given
        FilePrintStreamFactory factory = mock(FilePrintStreamFactory.class);

        final List<ByteArrayOutputStream> output = new ArrayList<ByteArrayOutputStream>();
        final File zebra = new File("target/zebra");

        CrossReference crossReference = new CrossReference() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                output.add(baos);
                return new OutputStreamWriter(baos);
            }

            @Override
            protected XrefRoot makeXRefRootNode() {
                return new MyXrefRoot();
            }

            @Override
            protected void xStreamAliasForXRefStory(XStream xstream) {
                xstream.alias("story", MyXrefStory.class);
            }

            @Override
            protected void xStreamAliasForXRefRoot(XStream xstream) {
                xstream.alias("xref", MyXrefRoot.class);
            }
        };

        StoryReporterBuilder builder = mock(StoryReporterBuilder.class);
        when(builder.outputDirectory()).thenReturn(zebra);
        FilePathResolver pathResolver = new ResolveToPackagedName();
        when(builder.pathResolver()).thenReturn(pathResolver);

        // When
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder), true);
        crossReference.getStepMonitor().stepMatchesPattern("a", true, new StepPattern("(def)", "[abc]"), Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder);

        System.out.println("CCC{" + output.get(0).toString() + "}CCC");

        // Then
        assertEquals("<xref>\n" +
                "  <meta>\n" +
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
                "</meta>\n" +
                "      <scenarios></scenarios>\n" +
                "      <passed>false</passed>\n" +
                "      <theme>testing</theme>\n" +
                "    </story>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <stepMatch>\n" +
                "      <annotatedPattern>(def)</annotatedPattern>\n" +
                "      <resolvedPattern>[abc]</resolvedPattern>\n" +
                "      <usages>\n" +
                "        <use>\n" +
                "          <story>/path/to/story</story>\n" +
                "          <scenario>I ask for a loan</scenario>\n" +
                "          <step>a</step>\n" +
                "        </use>\n" +
                "      </usages>\n" +
                "    </stepMatch>\n" +
                "  </stepMatches>\n" +
                "  <themes>\n" +
                "    <string>testing</string>\n" +
                "  </themes>\n" +
                "</xref>", output.get(0).toString()); // xml

        System.out.println("DDD{" + output.get(1).toString() + "}DDD");

        assertEquals("{'xref': {\n" +
                "  'meta': [\n" +
                "    'author=Mauro'\n" +
                "  ],\n" +
                "  'stories': [\n" +
                "    {\n" +
                "      'description': 'An interesting story',\n" +
                "      'narrative': 'In order to renovate my house\\u000aAs a customer\\u000aI want to get a loan\\u000a',\n" +
                "      'name': '/path/to/story',\n" +
                "      'path': '/path/to/story',\n" +
                "      'html': 'path.to.html',\n" +
                "      'meta': 'author=Mauro\\u000a',\n" +
                "      'scenarios': '',\n" +
                "      'passed': false,\n" +
                "      'theme': 'testing'\n" +
                "    }\n" +
                "  ],\n" +
                "  'stepMatches': [\n" +
                "    {\n" +
                "      'annotatedPattern': '(def)',\n" +
                "      'resolvedPattern': '[abc]',\n" +
                "      'usages': [\n" +
                "        {\n" +
                "          'story': '/path/to/story',\n" +
                "          'scenario': 'I ask for a loan',\n" +
                "          'step': 'a'\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  'themes': [\n" +
                "    'testing'\n" +
                "  ]\n" +
                "}}", output.get(1).toString().replace('\"', '\'')); // json

    }

    private static class MyXrefRoot extends CrossReference.XrefRoot {
        Set<String> themes = new HashSet<String>();
        public MyXrefRoot() {
            super();
        }

        @Override
        protected CrossReference.XrefStory makeXRefStoryNode(StoryReporterBuilder storyReporterBuilder, Story story, boolean passed) {
            return new MyXrefStory(story, storyReporterBuilder, passed, themes);
        }
    }

    private static class MyXrefStory extends CrossReference.XrefStory {
        private String theme;
        private transient Set<String> themes = new HashSet<String>();
        public MyXrefStory(Story story, StoryReporterBuilder storyReporterBuilder, boolean passed, Set<String> themes) {
            super(story, storyReporterBuilder, passed);
            this.themes = themes;
        }

        @Override
        protected String appendMetaProperty(String property, String meta) {
            if (property.startsWith("theme")) {
                this.theme = property.substring(property.indexOf("=")+1);
                themes.add(this.theme);
                return null;
            } else {
                return super.appendMetaProperty(property, meta);
            }
        }

        @Override
        protected void addMetaProperty(String property, Set<String> meta) {
            if (!property.startsWith("theme")) {
                super.addMetaProperty(property, meta);
            }
        }
    }


}
