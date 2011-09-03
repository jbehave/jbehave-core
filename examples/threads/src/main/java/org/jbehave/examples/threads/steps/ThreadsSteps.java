package org.jbehave.examples.threads.steps;

import java.util.concurrent.TimeUnit;

import org.jbehave.core.annotations.When;

public class ThreadsSteps {

    @When("$name counts to $n Mississippi")
    public void whenSomeoneCountsMississippis(String name, int n) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            System.out.println(name + " says " + i + " Mississippi");
            TimeUnit.SECONDS.sleep(i);
        }
    }

}
