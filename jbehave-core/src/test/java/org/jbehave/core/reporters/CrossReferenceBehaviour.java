package org.jbehave.core.reporters;

import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jbehave.core.model.StepPattern;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.jbehave.core.steps.StepType;
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
        final File zebra = new File("target/zebra");

        final long[] duration = new long[1];

        CrossReference crossReference = new CrossReference() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                output.add(baos);
                return new OutputStreamWriter(baos);
            }

            @Override
            protected void aliasForXRefRoot(XStream xstream) {
                xstream.alias("xref", XRefRootWithoutThemes.class);
            }

            @Override
            protected XRefRoot newXRefRoot() {
                return new XRefRootWithoutThemes(duration);
            }

            @Override
            public String getMetaFilter() {
                return "+color blue";
            }
        };
        crossReference.excludingStoriesWithNoExecutedScenarios(false);

        StoryReporterBuilder builder = mock(StoryReporterBuilder.class);
        when(builder.outputDirectory()).thenReturn(zebra);
        FilePathResolver pathResolver = new ResolveToPackagedName();
        when(builder.pathResolver()).thenReturn(pathResolver);

        // When
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder), true);
        crossReference.getStepMonitor().stepMatchesPattern("a", true, new StepPattern(StepType.GIVEN, "(def)", "[abc]"), Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON        
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder);

        // Then
        assertEquals("<xref>\n" +
                "  <whenMade>NUMBER</whenMade>\n" +
                "  <createdBy>JBehave</createdBy>\n" +
                "  <metaFilter>+color blue</metaFilter>\n" +
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
                "      <started>NUMBER</started>\n" +
                "      <duration>TIME</duration>\n" +
                "    </story>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <stepMatch>\n" +
                "      <type>GIVEN</type>\n" +
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
                "</xref>", output.get(0).toString().replaceAll("[0-9]{8,15}", "NUMBER").replaceAll("<duration>[0-9]*</duration>", "<duration>TIME</duration>")); // xml

        assertEquals("{'xref': {\n" +
                "  'whenMade': NUMBER,\n" +
                "  'createdBy': 'JBehave',\n" +
                "  'metaFilter': '+color blue',\n" +
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
                "      'passed': false,\n" +
                "      'started': NUMBER,\n" +
                "      'duration': TIME\n" +
                "    }\n" +
                "  ],\n" +
                "  'stepMatches': [\n" +
                "    {\n" +
                "      'type': 'GIVEN',\n" +
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
                "}}", output.get(1).toString().replace('\"', '\'').replaceAll("[0-9]{8,15}", "NUMBER").replaceAll("duration': [0-9]*", "duration': TIME")); // json
        assertEquals(2, output.size());

    }

    @Test
    public void shouldProduceXmlOutputsOfStoriesAndSteps() throws Exception {

        // Given
        FilePrintStreamFactory factory = mock(FilePrintStreamFactory.class);

        final List<ByteArrayOutputStream> output = new ArrayList<ByteArrayOutputStream>();
        final File zebra = new File("target/zebra");

        final long[] duration = new long[1];

        CrossReference crossReference = new CrossReference() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                output.add(baos);
                return new OutputStreamWriter(baos);
            }

            @Override
            protected void aliasForXRefRoot(XStream xstream) {
                xstream.alias("xref", XRefRootWithoutThemes.class);
            }

            @Override
            protected XRefRoot newXRefRoot() {
                return new XRefRootWithoutThemes(duration);
            }

            @Override
            public String getMetaFilter() {
                return "+color blue";
            }


        }.withXmlOnly();
        crossReference.excludingStoriesWithNoExecutedScenarios(false);

        StoryReporterBuilder builder = mock(StoryReporterBuilder.class);
        when(builder.outputDirectory()).thenReturn(zebra);
        FilePathResolver pathResolver = new ResolveToPackagedName();
        when(builder.pathResolver()).thenReturn(pathResolver);

        // When
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder), true);
        crossReference.getStepMonitor().stepMatchesPattern("a", true, new StepPattern(StepType.GIVEN, "(def)", "[abc]"), Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder);

        // Then
        assertEquals("<xref>\n" +
                "  <whenMade>NUMBER</whenMade>\n" +
                "  <createdBy>JBehave</createdBy>\n" +
                "  <metaFilter>+color blue</metaFilter>\n" +
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
                "      <started>NUMBER</started>\n" +
                "      <duration>TIME</duration>\n" +
                "    </story>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <stepMatch>\n" +
                "      <type>GIVEN</type>\n" +
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
                "</xref>", output.get(0).toString().replaceAll("[0-9]{8,15}", "NUMBER").replaceAll("<duration>[0-9]*</duration>", "<duration>TIME</duration>")); // xml

        assertEquals(1, output.size());



    }

    @Test
    public void shouldProduceJsonOutputsOfStoriesAndSteps() throws Exception {

        // Given
        FilePrintStreamFactory factory = mock(FilePrintStreamFactory.class);

        final List<ByteArrayOutputStream> output = new ArrayList<ByteArrayOutputStream>();
        final File zebra = new File("target/zebra");

        final long[] duration = new long[1];

        CrossReference crossReference = new CrossReference() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                output.add(baos);
                return new OutputStreamWriter(baos);
            }

            @Override
            protected void aliasForXRefRoot(XStream xstream) {
                xstream.alias("xref", XRefRootWithoutThemes.class);
            }

            @Override
            protected XRefRoot newXRefRoot() {
                return new XRefRootWithoutThemes(duration);
            }

            @Override
            public String getMetaFilter() {
                return "+color blue";
            }


        }.withJsonOnly();
        crossReference.excludingStoriesWithNoExecutedScenarios(false);

        StoryReporterBuilder builder = mock(StoryReporterBuilder.class);
        when(builder.outputDirectory()).thenReturn(zebra);
        FilePathResolver pathResolver = new ResolveToPackagedName();
        when(builder.pathResolver()).thenReturn(pathResolver);

        // When
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder), true);
        crossReference.getStepMonitor().stepMatchesPattern("a", true, new StepPattern(StepType.GIVEN, "(def)", "[abc]"), Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder);

        // Then

        assertEquals("{'xref': {\n" +
                "  'whenMade': NUMBER,\n" +
                "  'createdBy': 'JBehave',\n" +
                "  'metaFilter': '+color blue',\n" +
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
                "      'passed': false,\n" +
                "      'started': NUMBER,\n" +
                "      'duration': 1\n" +
                "    }\n" +
                "  ],\n" +
                "  'stepMatches': [\n" +
                "    {\n" +
                "      'type': 'GIVEN',\n" +
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
                "}}", output.get(0).toString().replace('\"', '\'').replaceAll("[0-9]{8,15}", "NUMBER").replace("duration': 0", "duration': 1")); // json

        assertEquals(1, output.size());

    }

    @Test
    public void shouldAllowOverridingOfObjectsCreated() throws Exception {

        // Given
        FilePrintStreamFactory factory = mock(FilePrintStreamFactory.class);

        final List<ByteArrayOutputStream> output = new ArrayList<ByteArrayOutputStream>();
        final File zebra = new File("target/zebra");

        final long[] duration = new long[1];


        CrossReference crossReference = new CrossReference() {
            @Override
            protected OutputStreamWriter makeWriter(File file) throws IOException {
                assertTrue(file.getCanonicalPath().contains("zebra"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                output.add(baos);
                return new OutputStreamWriter(baos);
            }


            @Override
            protected XRefRoot newXRefRoot() {
                return new XRefRootWithThemes(duration);
            }


            @Override
            protected void aliasForXRefStory(XStream xstream) {
                xstream.alias("story", XRefStoryWithTheme.class);
            }

            @Override
            protected void aliasForXRefRoot(XStream xstream) {
                xstream.alias("xref", XRefRootWithThemes.class);
            }
        };
        crossReference.excludingStoriesWithNoExecutedScenarios(false);

        StoryReporterBuilder builder = mock(StoryReporterBuilder.class);
        when(builder.outputDirectory()).thenReturn(zebra);
        FilePathResolver pathResolver = new ResolveToPackagedName();
        when(builder.pathResolver()).thenReturn(pathResolver);

        // When
        PrintStreamOutputBehaviour.narrateAnInterestingStory(crossReference.createStoryReporter(factory, builder), true);
        crossReference.getStepMonitor().stepMatchesPattern("a", true, new StepPattern(StepType.GIVEN, "(def)", "[abc]"), Object.class.getDeclaredMethods()[0], new Object());

        // generate XML and JSON
        verifyNoMoreInteractions(factory, builder);
        crossReference.outputToFiles(builder);

        // Then
        assertEquals("<xref>\n" +
                "  <whenMade>NUMBER</whenMade>\n" +
                "  <createdBy>JBehave</createdBy>\n" +
                "  <metaFilter></metaFilter>\n" +
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
                "      <started>NUMBER</started>\n" +
                "      <duration>1</duration>\n" +
                "      <theme>testing</theme>\n" +
                "    </story>\n" +
                "  </stories>\n" +
                "  <stepMatches>\n" +
                "    <stepMatch>\n" +
                "      <type>GIVEN</type>\n" +
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
                "</xref>", output.get(0).toString().replaceAll("[0-9]{8,15}", "NUMBER").replace("<duration>0</duration>", "<duration>1</duration>")); // xml

        assertEquals("{'xref': {\n" +
                "  'whenMade': NUMBER,\n" +
                "  'createdBy': 'JBehave',\n" +
                "  'metaFilter': '',\n" +
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
                "      'started': NUMBER,\n" +
                "      'duration': 1,\n" +
                "      'theme': 'testing'\n" +
                "    }\n" +
                "  ],\n" +
                "  'stepMatches': [\n" +
                "    {\n" +
                "      'type': 'GIVEN',\n" +
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
                "}}", output.get(1).toString().replace('\"', '\'').replaceAll("[0-9]{8,15}", "NUMBER").replace("duration': 0", "duration': 1")); // json

    }

    private static class XRefRootWithThemes extends CrossReference.XRefRoot {
        Set<String> themes = new HashSet<String>();

        private transient long[] duration;

        public XRefRootWithThemes(long[] duration) {
            this.duration = duration;
        }

        @Override
        protected Long getTime(Map<String, Long> times, Story story) {
            long d = super.getTime(times, story);
            duration[0] = d;
            return d;
        }

        @Override
        protected CrossReference.XRefStory createXRefStory(StoryReporterBuilder storyReporterBuilder, Story story, boolean passed) {
            return new XRefStoryWithTheme(story, storyReporterBuilder, passed, themes);
        }
    }

    private static class XRefRootWithoutThemes extends CrossReference.XRefRoot {

        private transient long[] duration;

        private XRefRootWithoutThemes(long[] duration) {
            this.duration = duration;
        }

        @Override
        protected Long getTime(Map<String, Long> times, Story story) {
            long d = super.getTime(times, story);
            duration[0] = d;
            return d;
        }


    }

    private static class XRefStoryWithTheme extends CrossReference.XRefStory {
        private String theme;
        private transient Set<String> themes = new HashSet<String>();
        public XRefStoryWithTheme(Story story, StoryReporterBuilder storyReporterBuilder, boolean passed, Set<String> themes) {
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
