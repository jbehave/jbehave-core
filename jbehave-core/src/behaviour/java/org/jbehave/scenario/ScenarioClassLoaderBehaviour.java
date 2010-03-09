package org.jbehave.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.junit.Test;

public class ScenarioClassLoaderBehaviour {

    @Test
    public void canInstantiateNewScenarioWithDefaultConstructor() throws MalformedURLException {
        ScenarioClassLoader classLoader = new ScenarioClassLoader(Arrays.<String>asList());
        String scenarioClassName = MyScenario.class.getName();
        assertScenarioIsInstantiated(classLoader, scenarioClassName);
    }

    @Test
    public void canInstantiateNewScenarioWithClassLoader() throws MalformedURLException {
        ScenarioClassLoader classLoader = new ScenarioClassLoader(Arrays.<String>asList());
        String scenarioClassName = MyScenario.class.getName();
        assertScenarioIsInstantiated(classLoader, scenarioClassName, ClassLoader.class);
    }

    private void assertScenarioIsInstantiated(ScenarioClassLoader classLoader, String scenarioClassName, Class<?>... parameterTypes) {
        RunnableScenario scenario = classLoader.newScenario(scenarioClassName);
        assertNotNull(scenario);
        assertEquals(scenarioClassName, scenario.getClass().getName());
    }

    private static class MyScenario extends JUnitScenario {

    	public MyScenario(){
            
        }

        public MyScenario(ClassLoader classLoader){
            
        }
    }

}
