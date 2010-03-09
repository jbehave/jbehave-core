package org.jbehave.examples.trader.spring;

import org.jbehave.examples.trader.TraderScenario;
import org.jbehave.scenario.RunnableScenario;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.spring.SpringStepsFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTraderScenario extends TraderScenario {

    public SpringTraderScenario(Class<? extends RunnableScenario> scenarioClass) {
        super(scenarioClass);
    }

    @Override
    protected CandidateSteps[] createSteps(StepsConfiguration configuration) {
        ListableBeanFactory parent = createBeanFactory();
        return new SpringStepsFactory(configuration, parent).createCandidateSteps();
    }

    private ListableBeanFactory createBeanFactory() {
        return (ListableBeanFactory) new ClassPathXmlApplicationContext(
                new String[] { "org/jbehave/examples/trader/spring/steps.xml" });
    }

}
