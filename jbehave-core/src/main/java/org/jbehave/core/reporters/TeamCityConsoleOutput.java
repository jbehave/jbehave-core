package org.jbehave.core.reporters;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.Timing;

/**
 * Decorates console output to allow TeamCity build script interaction: *
 * https://confluence.jetbrains.com/display/TCD9/Build+Script+Interaction+with+TeamCity
 * 
 * <p>Scenarios are interpreted as TeamCity tests. Pending scenarios are considered as ignored.</p>
 */
public class TeamCityConsoleOutput extends ConsoleOutput {

    private static final String TEAMCITY_EVENT = "##teamcity[{0} name=''{1}'']\n";
    private static final String TEAMCITY_EVENT_MESSAGE = "##teamcity[{0} name=''{1}'' message=''{2}'']\n";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMdd-HH:mm:ss");

    private String eventName;
    private Keywords keywords;

    public TeamCityConsoleOutput() {
        this(new LocalizedKeywords());
    }

    public TeamCityConsoleOutput(Keywords keywords) {
        super(keywords);
        this.keywords = keywords;
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        this.eventName = keywords.scenario() + scenarioTitle(scenario);
        print(format("testStarted", eventName));
        super.beforeScenario(scenario);
    }

    @Override
    public void afterScenario(Timing timing) {
        super.afterScenario(timing);
        print(format("testFinished", eventName));
        this.eventName = null;
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        print(format("testIgnored", eventName));
    }

    @Override
    public void failed(String step, Throwable storyFailure) {
        super.failed(step, storyFailure);
        print(format("testFailed", eventName, storyFailure.getMessage()));
    }

    private String format(String event, String name) {
        return MessageFormat.format(TEAMCITY_EVENT, event, name);
    }

    private String format(String event, String name, String message) {
        return MessageFormat.format(TEAMCITY_EVENT_MESSAGE, event, name,
                message);
    }

    private String scenarioTitle(Scenario scenario) {
        String scenarioTitle = scenario.getTitle();
        if (StringUtils.isEmpty(scenarioTitle)) {
            scenarioTitle = "scenario-" + DATE_FORMAT.format(new Date());
        }
        return scenarioTitle;
    }

}
