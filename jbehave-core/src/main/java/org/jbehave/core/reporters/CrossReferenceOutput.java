package org.jbehave.core.reporters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import org.jbehave.core.model.*;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepMonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossReferenceOutput extends Format {

    private StepMonitor stepMonitor = new StepMonitor.NULL() {
        public void stepMatchesPattern(String step, boolean matches, String pattern, Method method, Object stepsInstance) {
            if (matches) {
                stepMatches.add(new StepMatch(currStoryPath, currScenarioTitle, step, pattern));
            }
            super.stepMatchesPattern(step, matches, pattern, method, stepsInstance);
        }
    };

    public String currStoryPath;
    public String currScenarioTitle;
    private List<Story> stories = new ArrayList<Story>();
    private List<StepMatch> stepMatches = new ArrayList<StepMatch>();

    private static class Root {
        private Set<String> meta = new HashSet<String>();
        private List<Stori> stories = new ArrayList<Stori>();
        private List<StepMatch> stepMatches = new ArrayList<StepMatch>();

        public Root(List<StepMatch> stepMatches, List<Story> stories) {
            this.stepMatches = stepMatches;
            for (Story aStory : stories) {
                this.stories.add(new Stori(aStory, this));
            }
        }
    }

    public StepMonitor getStepMonitor() {
        return stepMonitor;
    }

    @SuppressWarnings("unused")
    private static class Stori {
        private String description;
        private String narrative = "";
        private String name;
        private String path;
        private String meta = "";
        private String scenarios = "";

        public Stori(Story story, Root root) {
            Narrative narrative = story.getNarrative();
            if (!narrative.isEmpty()) {
                this.narrative = "In order to " + narrative.inOrderTo() + "\n" +
                        "As a " + narrative.asA() + "\n" +
                        "I want to " + narrative.iWantTo() + "\n";
            }
            this.description = story.getDescription().asString();
            this.name = story.getName();
            this.path = story.getPath();
            Meta meta1 = story.getMeta();
            for (String next : meta1.getPropertyNames()) {
                String s = meta + next + "=" + meta1.getProperty(next);
                root.meta.add(s);
                meta = s + "\n";

            }
            List<Scenario> scenarios1 = story.getScenarios();
            for (Scenario scenario : scenarios1) {
                String body = "Scenario:" + scenario.getTitle() + "\n";
                List<String> steps = scenario.getSteps();
                for (String s : steps) {
                    body = body + s + "\n";
                }
                scenarios = scenarios + body + "\n\n";
            }
        }
    }

    @SuppressWarnings("unused")
    private static class StepMatch {
        private final String storyPath;
        private final String scenarioTitle;
        private final String step;
        private final String pattern;

        public StepMatch(String storyPath, String scenarioTitle, String step, String pattern) {
            this.storyPath = storyPath;
            this.scenarioTitle = scenarioTitle;
            this.step = step;
            this.pattern = pattern;
        }
    }

    private SilentStepMonitor delegateStepMonitor = new SilentStepMonitor();

    public CrossReferenceOutput() {
        super("cross-reference collecting");
    }

    public void outputToFiles(StoryReporterBuilder storyReporterBuilder) throws IOException {
        Root root = new Root(stepMatches, stories);
        try {
            outputFile("xref.xml", new XStream(), storyReporterBuilder, root);
            outputFile("xref.json", new XStream(new JsonHierarchicalStreamDriver()), storyReporterBuilder, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputFile(String name, XStream xstream, StoryReporterBuilder storyReporterBuilder, Root root) throws IOException {
        File outputDir = storyReporterBuilder.outputDirectory();
        outputDir.mkdirs();
        OutputStreamWriter writer = makeWriter(new File(outputDir, name));
        writer.write(configure(xstream).toXML(root));
        writer.flush();
        writer.close();

    }

    protected OutputStreamWriter makeWriter(File file) throws IOException {
        return new FileWriter(file);
    }

    private XStream configure(XStream xstream) {
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("xref", Root.class);
        xstream.alias("StepMatch", StepMatch.class);
        xstream.alias("Story", Story.class);
        xstream.alias("Scenario", Scenario.class);
        xstream.omitField(ExamplesTable.class, "parameterConverters");
        xstream.omitField(ExamplesTable.class, "defaults");
        return xstream;
    }

    @Override
    public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
        return new NullStoryReporter() {

            @Override
            public void beforeStory(Story story, boolean givenStory) {
                stories.add(story);
                currStoryPath = story.getPath();
            }

            @Override
            public void beforeScenario(String title) {
                currScenarioTitle = title;
            }
        };
    }

}
