package org.jbehave.core.reporters;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Story;

/**
 * <p>
 * Reporter that collects statistics and writes them as properties to output
 * stream after each story
 * </p>
 */
public class PostStoryStatisticsCollector implements StoryReporter {

	private final OutputStream output;
	private final Map<String, Integer> data = new HashMap<String, Integer>();
	private final List<String> events = asList("steps", "stepsSuccessful",
			"stepsIgnorable", "stepsPending", "stepsNotPerformed",
			"stepsFailed", "scenarios", "scenariosFailed", "givenStories",
			"examples");

	private Throwable cause;

	public PostStoryStatisticsCollector(OutputStream output) {
		this.output = output;
	}

	public void successful(String step) {
		count("steps");
		count("stepsSuccessful");
	}

	public void ignorable(String step) {
		count("steps");
		count("stepsIgnorable");
	}

	public void pending(String step) {
		count("steps");
		count("stepsPending");
	}

	public void notPerformed(String step) {
		count("steps");
		count("stepsNotPerformed");
	}

	public void failed(String step, Throwable cause) {
		this.cause = cause;
		count("steps");
		count("stepsFailed");
	}

	public void beforeStory(Story story, boolean embeddedStory) {
		resetData();
	}

	public void afterStory(boolean embeddedStory) {
		writeData();
	}

	public void givenStories(List<String> storyPaths) {
		count("givenStories");
	}

	public void beforeScenario(String title) {
		cause = null;
	}

	public void afterScenario() {
		count("scenarios");
		if (cause != null) {
			count("scenariosFailed");
		}
	}

	public void beforeExamples(List<String> steps, ExamplesTable table) {
	}

	public void example(Map<String, String> tableRow) {
		count("examples");
	}

	public void afterExamples() {
	}
	
	public void dryRun() {		
	}

	private void count(String event) {
		Integer count = data.get(event);
		if (count == null) {
			count = 0;
		}
		count++;
		data.put(event, count);
	}

	private void writeData() {
		Properties p = new Properties();
		for (String event : data.keySet()) {
			p.setProperty(event, data.get(event).toString());
		}
		try {
			p.store(output, this.getClass().getName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void resetData() {
		data.clear();
		for (String event : events) {
			data.put(event, 0);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append(output).append(data).toString();
	}


}
