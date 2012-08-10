package org.jbehave.core.reporters;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

import static org.jbehave.core.steps.StepCreator.PARAMETER_TABLE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_TABLE_START;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

/**
 * <p>
 * Story reporter that outputs to a template.
 * </p>
 */
public class TemplateableOutput implements StoryReporter {

    private final File file;
    private final Keywords keywords;
    private final TemplateProcessor processor;
    private final String templatePath;
    private OutputStory outputStory = new OutputStory();
    private OutputScenario outputScenario = new OutputScenario();
    private OutputStep failedStep;

    public TemplateableOutput(File file, Keywords keywords, TemplateProcessor processor, String templatePath) {
        this.file = file;
        this.keywords = keywords;
        this.processor = processor;
        this.templatePath = templatePath;
    }

    public void storyNotAllowed(Story story, String filter) {
        this.outputStory.notAllowedBy = filter;
    }

    public void beforeStory(Story story, boolean givenStory) {
        if (!givenStory) {
            this.outputStory = new OutputStory();
            this.outputStory.description = story.getDescription().asString();
            this.outputStory.path = story.getPath();
        }
        if (!story.getMeta().isEmpty()) {
            this.outputStory.meta = new OutputMeta(story.getMeta());
        }
    }

    public void narrative(Narrative narrative) {
        if (!narrative.isEmpty()) {
            this.outputStory.narrative = new OutputNarrative(narrative);
        }
    }

    public void scenarioNotAllowed(Scenario scenario, String filter) {
        this.outputScenario.notAllowedBy = filter;
    }

    public void beforeScenario(String title) {
        if (this.outputScenario.currentExample == null) {
            this.outputScenario = new OutputScenario();
        }
        this.outputScenario.title = title;
    }

    public void beforeStep(String step) {
    }

    public void successful(String step) {
        this.outputScenario.addStep(new OutputStep(step, "successful"));
    }

    public void ignorable(String step) {
        this.outputScenario.addStep(new OutputStep(step, "ignorable"));
    }

    public void pending(String step) {
        this.outputScenario.addStep(new OutputStep(step, "pending"));
    }

    public void notPerformed(String step) {
        this.outputScenario.addStep(new OutputStep(step, "notPerformed"));
    }

    public void failed(String step, Throwable storyFailure) {
        this.failedStep = new OutputStep(step, "failed");
        failedStep.failure = storyFailure;
        this.outputScenario.addStep(failedStep);
    }

    public void failedOutcomes(String step, OutcomesTable table) {
        failed(step, table.failureCause());
        this.failedStep.outcomes = table;
    }

    public void givenStories(GivenStories givenStories) {
        if (!givenStories.getStories().isEmpty()) {
            this.outputScenario.givenStories = givenStories;
        }
    }

    public void givenStories(List<String> storyPaths) {
        givenStories(new GivenStories(StringUtils.join(storyPaths, ",")));
    }

    public void scenarioMeta(Meta meta) {
        if (!meta.isEmpty()) {
            this.outputScenario.meta = new OutputMeta(meta);
        }
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
        this.outputScenario.examplesSteps = steps;
        this.outputScenario.examplesTable = table;
    }

    public void example(Map<String, String> parameters) {
        this.outputScenario.examples.add(parameters);
        this.outputScenario.currentExample = parameters;
    }

    public void afterExamples() {
        this.outputScenario.currentExample = null;
    }

    public void dryRun() {
    }

    public void afterScenario() {
        if (this.outputScenario.currentExample == null) {
            this.outputStory.scenarios.add(outputScenario);
        }
    }

    public void pendingMethods(List<String> methods) {
        this.outputStory.pendingMethods = methods;
    }

    public void restarted(String step, Throwable cause) {
        this.outputScenario.addStep(new OutputRestart(step, cause.getMessage()));
    }

    public void storyCancelled(Story story, StoryDuration storyDuration) {
        this.outputStory.cancelled = true;
        this.outputStory.storyDuration = storyDuration;
    }

    public void afterStory(boolean givenStory) {
        if (!givenStory) {
            Map<String, Object> model = newDataModel();
            model.put("story", outputStory);
            model.put("keywords", new OutputKeywords(keywords));

            BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
            TemplateHashModel enumModels = wrapper.getEnumModels();
            TemplateHashModel escapeEnums;
            try {
                String escapeModeEnum = EscapeMode.class.getCanonicalName();
                escapeEnums = (TemplateHashModel) enumModels.get(escapeModeEnum);
                model.put("EscapeMode", escapeEnums);  
            } catch (TemplateModelException e) {
                throw new IllegalArgumentException(e);
            }  

            write(file, templatePath, model);
        }
    }

    private File write(File file, String resource, Map<String, Object> dataModel) {
        try {
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            processor.process(resource, dataModel, writer);
            return file;
        } catch (Exception e) {
            throw new RuntimeException(resource, e);
        }
    }

    private Map<String, Object> newDataModel() {
        return new HashMap<String, Object>();
    }

    public static class OutputKeywords {

        private final Keywords keywords;

        public OutputKeywords(Keywords keywords) {
            this.keywords = keywords;
        }

        public String getMeta() {
            return keywords.meta();
        }

        public String getMetaProperty() {
            return keywords.metaProperty();
        }

        public String getNarrative() {
            return keywords.narrative();
        }

        public String getInOrderTo() {
            return keywords.inOrderTo();
        }

        public String getAsA() {
            return keywords.asA();
        }

        public String getiWantTo() {
            return keywords.iWantTo();
        }

        public String getScenario() {
            return keywords.scenario();
        }

        public String getGivenStories() {
            return keywords.givenStories();
        }

        public String getExamplesTable() {
            return keywords.examplesTable();
        }

        public String getExamplesTableRow() {
            return keywords.examplesTableRow();
        }

        public String getExamplesTableHeaderSeparator() {
            return keywords.examplesTableHeaderSeparator();
        }

        public String getExamplesTableValueSeparator() {
            return keywords.examplesTableValueSeparator();
        }

        public String getExamplesTableIgnorableSeparator() {
            return keywords.examplesTableIgnorableSeparator();
        }

        public String getGiven() {
            return keywords.given();
        }

        public String getWhen() {
            return keywords.when();
        }

        public String getThen() {
            return keywords.then();
        }

        public String getAnd() {
            return keywords.and();
        }

        public String getIgnorable() {
            return keywords.ignorable();
        }

        public String getPending() {
            return keywords.pending();
        }

        public String getNotPerformed() {
            return keywords.notPerformed();
        }

        public String getFailed() {
            return keywords.failed();
        }

        public String getDryRun() {
            return keywords.dryRun();
        }

        public String getStoryCancelled() {
            return keywords.storyCancelled();
        }

        public String getDuration() {
            return keywords.duration();
        }

        public String getYes() {
            return keywords.yes();
        }

        public String getNo() {
            return keywords.no();
        }
    }

    public static class OutputStory {
        private String description;
        private String path;
        private OutputMeta meta;
        private OutputNarrative narrative;
        private String notAllowedBy;
        private List<String> pendingMethods;
        private List<OutputScenario> scenarios = new ArrayList<OutputScenario>();
        private boolean cancelled;
        private StoryDuration storyDuration;

        public String getDescription() {
            return description;
        }

        public String getPath() {
            return path;
        }

        public OutputMeta getMeta() {
            return meta;
        }

        public OutputNarrative getNarrative() {
            return narrative;
        }

        public String getNotAllowedBy() {
            return notAllowedBy;
        }

        public List<String> getPendingMethods() {
            return pendingMethods;
        }

        public List<OutputScenario> getScenarios() {
            return scenarios;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public StoryDuration getStoryDuration() {
            return storyDuration;
        }
    }

    public static class OutputMeta {

        private final Meta meta;

        public OutputMeta(Meta meta) {
            this.meta = meta;
        }

        public Map<String, String> getProperties() {
            Map<String, String> properties = new HashMap<String, String>();
            for (String name : meta.getPropertyNames()) {
                properties.put(name, meta.getProperty(name));
            }
            return properties;
        }

    }

    public static class OutputNarrative {
        private final Narrative narrative;

        public OutputNarrative(Narrative narrative) {
            this.narrative = narrative;
        }

        public String getInOrderTo() {
            return narrative.inOrderTo();
        }

        public String getAsA() {
            return narrative.asA();
        }

        public String getiWantTo() {
            return narrative.iWantTo();
        }

    }

    public static class OutputScenario {
        private String title;
        private List<OutputStep> steps = new ArrayList<OutputStep>();
        private OutputMeta meta;
        private GivenStories givenStories;
        private String notAllowedBy;
        private List<String> examplesSteps;
        private ExamplesTable examplesTable;
        private Map<String, String> currentExample;
        private List<Map<String, String>> examples = new ArrayList<Map<String, String>>();
        private Map<Map<String, String>, List<OutputStep>> stepsByExample = new HashMap<Map<String, String>, List<OutputStep>>();

        public String getTitle() {
            return title;
        }

        public void addStep(OutputStep outputStep) {
            if (examplesTable == null) {
                steps.add(outputStep);
            } else {
                List<OutputStep> currentExampleSteps = stepsByExample.get(currentExample);
                if (currentExampleSteps == null) {
                    currentExampleSteps = new ArrayList<OutputStep>();
                    stepsByExample.put(currentExample, currentExampleSteps);
                }
                currentExampleSteps.add(outputStep);
            }
        }

        public List<OutputStep> getSteps() {
            return steps;
        }

        public List<OutputStep> getStepsByExample(Map<String, String> example) {
            List<OutputStep> steps = stepsByExample.get(example);
            if (steps == null) {
                return new ArrayList<OutputStep>();
            }
            return steps;
        }

        public OutputMeta getMeta() {
            return meta;
        }

        public GivenStories getGivenStories() {
            return givenStories;
        }

        public String getNotAllowedBy() {
            return notAllowedBy;
        }

        public List<String> getExamplesSteps() {
            return examplesSteps;
        }

        public ExamplesTable getExamplesTable() {
            return examplesTable;
        }

        public List<Map<String, String>> getExamples() {
            return examples;
        }
    }

    public static class OutputRestart extends OutputStep {

        public OutputRestart(String step, String outcome) {
            super(step, outcome);
        }

    }

    public static class OutputStep {
        private final String step;
        private final String outcome;
        private Throwable failure;
        private OutcomesTable outcomes;
        private List<OutputParameter> parameters;
        private String stepPattern;
        private String tableAsString;
        private ExamplesTable table;

        public OutputStep(String step, String outcome) {
            this.step = step;
            this.outcome = outcome;
            parseTableAsString();
            parseParameters();
            createStepPattern();
        }

        public String getStep() {
            return step;
        }

        public String getStepPattern() {
            return stepPattern;
        }

        public List<OutputParameter> getParameters() {
            return parameters;
        }

        public String getOutcome() {
            return outcome;
        }

        public Throwable getFailure() {
            return failure;
        }

        public String getFailureCause() {
            if (failure != null) {
                return new StackTraceFormatter(true).stackTrace(failure);
            }
            return "";
        }

        public ExamplesTable getTable() {
            return table;
        }

        public OutcomesTable getOutcomes() {
            return outcomes;
        }

        public String getOutcomesFailureCause() {
            if (outcomes.failureCause() != null) {
                return new StackTraceFormatter(true).stackTrace(outcomes.failureCause());
            }
            return "";
        }

        /*
         * formatting without escaping doesn't make sense unless
         * we do a ftl text output format
         */
        @Deprecated
        public String getFormattedStep(String parameterPattern) {
            return getFormattedStep(EscapeMode.NONE, parameterPattern);
        }

        public String getFormattedStep(EscapeMode outputFormat, String parameterPattern) {
            // note that escaping the stepPattern string only works
            // because placeholders for parameters do not contain
            // special chars (the placeholder is {0} etc)
            String escapedStep = escapeString(outputFormat, stepPattern);
            System.out.println("escape: "+stepPattern+" "+escapedStep);
            if (!parameters.isEmpty()) {
                try {
                    return MessageFormat.format(escapedStep, formatParameters(outputFormat, parameterPattern));
                } catch (RuntimeException e) {
                    throw new StepFormattingFailed(stepPattern, parameterPattern, parameters, e);
                }
            }
            return escapedStep;
        }

        private String escapeString(EscapeMode outputFormat, String string) {
            if(outputFormat==EscapeMode.HTML) {
                return StringEscapeUtils.escapeHtml(string);
            } else if(outputFormat==EscapeMode.XML) {
                return StringEscapeUtils.escapeXml(string);
            } else {
                return string;
            }
        }

        private Object[] formatParameters(EscapeMode outputFormat, String parameterPattern) {
            Object[] arguments = new Object[parameters.size()];
            for (int a = 0; a < parameters.size(); a++) {
                arguments[a] = MessageFormat.format(parameterPattern, escapeString(outputFormat, parameters.get(a).getValue()));
            }
            return arguments;
        }

        private void parseParameters() {
            // first, look for parameterized scenarios
            parameters = findParameters(PARAMETER_VALUE_START + PARAMETER_VALUE_START, PARAMETER_VALUE_END
                    + PARAMETER_VALUE_END);
            // second, look for normal scenarios
            if (parameters.isEmpty()) {
                parameters = findParameters(PARAMETER_VALUE_START, PARAMETER_VALUE_END);
            }
        }

        private List<OutputParameter> findParameters(String start, String end) {
            List<OutputParameter> parameters = new ArrayList<OutputParameter>();
            Matcher matcher = Pattern.compile("(" + start + ".*?" + end + ")(\\W|\\Z)",
                    Pattern.DOTALL).matcher(step);
            while (matcher.find()) {
                parameters.add(new OutputParameter(step, matcher.start(), matcher.end()));
            }
            return parameters;
        }

        private void parseTableAsString() {
            if (step.contains(PARAMETER_TABLE_START) && step.contains(PARAMETER_TABLE_END)) {
                tableAsString = StringUtils.substringBetween(step, PARAMETER_TABLE_START, PARAMETER_TABLE_END);
                table = new ExamplesTable(tableAsString);
            }
        }

        private void createStepPattern() {
            this.stepPattern = step;
            if (tableAsString != null) {
                this.stepPattern = StringUtils.replaceOnce(stepPattern, PARAMETER_TABLE_START + tableAsString
                        + PARAMETER_TABLE_END, "");
            }
            for (int count = 0; count < parameters.size(); count++) {
                String value = parameters.get(count).toString();
                this.stepPattern = stepPattern.replace(value, "{" + count + "}");
            }
        }

        @SuppressWarnings("serial")
        public static class StepFormattingFailed extends RuntimeException {

            public StepFormattingFailed(String stepPattern, String parameterPattern, List<OutputParameter> parameters,
                    RuntimeException cause) {
                super("Failed to format step '" + stepPattern + "' with parameter pattern '" + parameterPattern
                        + "' and parameters: " + parameters, cause);
            }

        }

    }

    public static class OutputParameter {
        private final String parameter;

        public OutputParameter(String pattern, int start, int end) {
            this.parameter = pattern.substring(start, end).trim();
        }

        public String getValue() {
            String value = StringUtils.remove(parameter, PARAMETER_VALUE_START);
            value = StringUtils.remove(value, PARAMETER_VALUE_END);
            return value;
        }

        @Override
        public String toString() {
            return parameter;
        }
    }

}
