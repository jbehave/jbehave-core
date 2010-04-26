package org.jbehave.scenario.reporters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.STATS;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.TXT;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.jbehave.scenario.Scenario;
import org.jbehave.scenario.i18n.I18nKeyWords;
import org.jbehave.scenario.parser.ScenarioNameResolver;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.FilePrintStreamFactory.FileConfiguration;
import org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format;
import org.junit.Test;

public class ScenarioReporterBuilderBehaviour {


    @Test
    public void shouldBuildWithStatsByDefault() throws IOException {
        Class<MyScenario> scenarioClass = MyScenario.class;
        ScenarioNameResolver nameResolver = new UnderscoredCamelCaseResolver();
        FilePrintStreamFactory factory = new FilePrintStreamFactory(scenarioClass, nameResolver);
        ScenarioReporterBuilder builder = new ScenarioReporterBuilder(factory);

        // When
        ScenarioReporter reporter = builder.withDefaultFormats().build();
        
        // Then
        ensureThat(reporter instanceof DelegatingScenarioReporter);
        Map<Format, ScenarioReporter> delegates = builder.getDelegates();
        ensureThat(delegates.size(), equalTo(1));
        ensureThat(delegates.get(STATS) instanceof StatisticsScenarioReporter);
    }

    @Test
    public void shouldAllowOverrideOfDefaultFileDirectory() throws IOException {
        Class<MyScenario> scenarioClass = MyScenario.class;
        ScenarioNameResolver nameResolver = new UnderscoredCamelCaseResolver();
        FilePrintStreamFactory factory = new FilePrintStreamFactory(scenarioClass, nameResolver);
        ScenarioReporterBuilder builder = new ScenarioReporterBuilder(factory);

        // When
        String fileDirectory = "my-reports";
        builder.outputTo(fileDirectory);
        
        // Then
        ensureThat(builder.fileConfiguration("").getOutputDirectory().endsWith(fileDirectory));
    }

    @Test
    public void shouldBuildAndOverrideDefaultReporterForAGivenFormat() throws IOException {
        Class<MyScenario> scenarioClass = MyScenario.class;
        ScenarioNameResolver nameResolver = new UnderscoredCamelCaseResolver();
        FilePrintStreamFactory factory = new FilePrintStreamFactory(scenarioClass, nameResolver);
        final ScenarioReporter txtReporter = new PrintStreamScenarioReporter(factory.getPrintStream(), new Properties(),  new I18nKeyWords(), true);
        ScenarioReporterBuilder builder = new ScenarioReporterBuilder(factory){
               public ScenarioReporter reporterFor(Format format){
                       switch (format) {
                           case TXT:
                               factory.useConfiguration(new FileConfiguration("text"));
                               return txtReporter;
                            default:
                               return super.reporterFor(format);
                       }
                   }
        };
        
        // When
        ScenarioReporter reporter = builder.withDefaultFormats().with(TXT).build();
        
        // Then
        ensureThat(reporter instanceof DelegatingScenarioReporter);
        Map<Format, ScenarioReporter> delegates = builder.getDelegates();
        ensureThat(delegates.size(), equalTo(2));
        ensureThat(delegates.get(TXT), equalTo(txtReporter));
    }

    private static class MyScenario extends Scenario {

    }
}
