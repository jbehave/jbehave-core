package org.jbehave.core.parsers.gherkin;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.parser.Parser;

import java.util.List;
import java.util.regex.Matcher;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.parsers.StoryTransformer;
import org.jbehave.core.parsers.TransformingStoryParser;

public class GherkinStoryParser extends TransformingStoryParser {

	public GherkinStoryParser(){
		this(new RegexStoryParser());
	}

	public GherkinStoryParser(StoryParser delegate){
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

		public String transform(String storyAsText) {
			final StringBuffer out = new StringBuffer();

			Formatter formatter = new Formatter(){
				public void uri(String uri) {
					out.append(uri).append("\n");
				}

				public void feature(Feature feature) {
					out.append(feature.getName()).append("\n\n");
					String description = feature.getDescription();
					writeNarrative(description);
				}

				private void writeNarrative(String description) {
					Matcher findingNarrative = compile(".*" + keywords.narrative() + "(.*?)", DOTALL).matcher(description);
			        if (findingNarrative.matches()) {
			            String narrative = findingNarrative.group(1).trim();
			            Matcher findingElements = compile(".*" + keywords.inOrderTo() + "(.*)\\s*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo()
			                    + "(.*)", DOTALL).matcher(narrative);
			            if (findingElements.matches()) {
			                String inOrderTo = findingElements.group(1).trim();
			                String asA = findingElements.group(2).trim();
			                String iWantTo = findingElements.group(3).trim();
			                out.append(keywords.narrative()).append("\n");
			                out.append(keywords.inOrderTo()).append(" ").append(inOrderTo).append("\n");
			                out.append(keywords.asA()).append(" ").append(asA).append("\n");
			                out.append(keywords.iWantTo()).append(" ").append(iWantTo).append("\n\n");			                
			            }
			        }
				}

				public void background(Background background) {
				}

				public void scenario(Scenario scenario) {
					out.append(keywords.scenario()+scenario.getName()).append("\n\n");
				}

				public void scenarioOutline(ScenarioOutline scenarioOutline) {
					out.append(keywords.scenario()+scenarioOutline.getName()).append("\n\n");
				}

				public void examples(Examples examples) {
					out.append(keywords.examplesTable()+examples.getName()).append("\n");
					writeRows(examples.getRows());
				}

				public void step(Step step) {
					out.append(step.getKeyword()+step.getName()).append("\n");
					writeRows(step.getRows());
				}

				public void eof() {
				}

				public void syntaxError(String state, String event,
						List<String> legalEvents, String uri, Integer line) {
				}

				public void done() {
				}

				public void close() {
				}
				
				private void writeRows(List<? extends Row> rows) {
					if ( rows != null && rows.size() > 0 ){
						for ( Row row : rows ){
							out.append("|");
							for ( String c : row.getCells() ){
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
