package org.jbehave.core.parsers.gherkin;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.util.List;
import java.util.regex.Matcher;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.parsers.StoryTransformer;
import org.jbehave.core.parsers.TransformingStoryParser;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import gherkin.parser.Parser;

public class GherkinStoryParser extends TransformingStoryParser {

    public GherkinStoryParser() {
        this(new RegexStoryParser());
    }

    public GherkinStoryParser(StoryParser delegate) {
        super(delegate, new GherkinTransformer());
    }

    public static class GherkinTransformer implements StoryTransformer {

        private LocalizedKeywords keywords;
        
        public GherkinTransformer() {
            this(new LocalizedKeywords());
        }

        public GherkinTransformer(LocalizedKeywords keywords) {
            this.keywords = keywords;
        }

        @Override
        public String transform(String storyAsText) {
            final StringBuffer out = new StringBuffer();

            Formatter formatter = new Formatter() {
                @Override
                public void uri(String uri) {
                    out.append(uri).append("\n");
                }

                @Override
                public void feature(Feature feature) {
                    out.append(feature.getName()).append("\n\n");
                    writeNarrative(feature.getDescription());
                    writeMeta(feature.getTags());
                }

                private void writeMeta(List<Tag> tags) {
                    if (tags.isEmpty()) {
                        return;
                    }
                    out.append(keywords.meta()).append(" ");
                    for (Tag tag : tags) {
                        out.append(tag.getName()).append(" ");
                    }
                    out.append("\n");
                }

                private void writeNarrative(String description) {
                    boolean matches = false;
                    Matcher findingNarrative = compile(".*" + keywords.narrative() + "(.*?)", DOTALL).matcher(
                            description);
                    if (findingNarrative.matches()) {
                        String narrative = findingNarrative.group(1).trim();
                        matches = writeNarrativeWithDefaultSyntax(out, narrative);
                        if (!matches) {
                            matches = writeNarrativeWithAlternativeSyntax(out, narrative);
                        }
                    }
                    if (!matches) {
                        // if narrative format does not match, write description as part of story description
                        out.append(description);
                    }                   
                }

                @SuppressWarnings("checkstyle:LocalVariableName")
                private boolean writeNarrativeWithDefaultSyntax(final StringBuffer out, String narrative) {
                    boolean matches = false;
                    Matcher findingElements = compile(
                            ".*" + keywords.inOrderTo() + "(.*)\\s*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo()
                                    + "(.*)", DOTALL).matcher(narrative);
                    if (findingElements.matches()) {
                        String inOrderTo = findingElements.group(1).trim();
                        String asA = findingElements.group(2).trim();
                        String iWantTo = findingElements.group(3).trim();
                        out.append(keywords.narrative()).append("\n");
                        out.append(keywords.inOrderTo()).append(" ").append(inOrderTo).append("\n");
                        out.append(keywords.asA()).append(" ").append(asA).append("\n");
                        out.append(keywords.iWantTo()).append(" ").append(iWantTo).append("\n\n");
                        matches = true;
                    }
                    return matches;
                }

                @SuppressWarnings("checkstyle:LocalVariableName")
                private boolean writeNarrativeWithAlternativeSyntax(final StringBuffer out, String narrative) {
                    boolean matches = false;
                        Matcher findingElements = compile(
                                ".*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo() + "(.*)\\s*" + keywords.soThat()
                                        + "(.*)", DOTALL).matcher(narrative);
                    if (findingElements.matches()) {
                        String asA = findingElements.group(1).trim();
                        String iWantTo = findingElements.group(2).trim();
                        String soThat = findingElements.group(3).trim();
                        out.append(keywords.narrative()).append("\n")
                                .append(keywords.asA()).append(" ").append(asA).append("\n")
                                .append(keywords.iWantTo()).append(" ").append(iWantTo).append("\n\n")
                                .append(keywords.soThat()).append(" ").append(soThat).append("\n");
                        matches = true;
                    }
                    return matches;
                }

                @Override
                public void background(Background background) {
                    out.append(keywords.lifecycle())
                            .append(background.getName())
                            .append("\n")
                            .append(keywords.before())
                            .append("\n");
                }

                @Override
                public void scenario(Scenario scenario) {
                    out.append("\n")
                            .append(keywords.scenario())
                            .append(scenario.getName())
                            .append("\n\n");
                    writeMeta(scenario.getTags());
                }

                @Override
                public void scenarioOutline(ScenarioOutline scenarioOutline) {
                    out.append("\n")
                            .append(keywords.scenario())
                            .append(scenarioOutline.getName())
                            .append("\n\n");
                    writeMeta(scenarioOutline.getTags());
                }

                @Override
                public void examples(Examples examples) {
                    out.append("\n")
                            .append(keywords.examplesTable())
                            .append(examples.getName())
                            .append("\n");
                    writeRows(examples.getRows());
                }

                @Override
                public void step(Step step) {
                    out.append(step.getKeyword())
                            .append(step.getName())
                            .append("\n");
                    writeRows(step.getRows());
                }

                @Override
                public void eof() {
                }

                @Override
                public void syntaxError(String state, String event,
                        List<String> legalEvents, String uri, Integer line) {
                }

                @Override
                public void done() {
                }

                @Override
                public void close() {
                }
                
                private void writeRows(List<? extends Row> rows) {
                    if (rows != null && rows.size() > 0) {
                        for (Row row : rows) {
                            out.append("|");
                            for (String c : row.getCells()) {
                                out.append(c).append("|");
                            }
                            out.append("\n");
                        }
                    }
                }

            };
            new Parser(formatter).parse(storyAsText, "", 0);
            return out.toString();
        }
    }

}
