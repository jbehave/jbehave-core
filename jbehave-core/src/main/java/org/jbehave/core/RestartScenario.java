package org.jbehave.core;

public class RestartScenario extends Error {
    public RestartScenario(String why) {
        super(why);
    }
}
