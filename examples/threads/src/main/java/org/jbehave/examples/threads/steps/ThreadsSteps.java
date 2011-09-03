package org.jbehave.examples.threads.steps;

import java.util.concurrent.TimeUnit;

import org.jbehave.core.annotations.When;

public class ThreadsSteps {

    @When("$name counts to $n Mississippi")
    public void whenSomeoneCountsMississippis(String name, int n) {
        for (int i = 0; i < n; i++) {
            System.out.println(name + " says " + i + " Mississippi");
            sleepFor(1, TimeUnit.SECONDS);
        }
    }

    private void sleepFor(int i, TimeUnit unit) {
        try {
            unit.sleep(i);
        } catch (InterruptedException e) {
            System.out.println("Yawn, who's interrupting my sleep?");
        }        
    }

    @When("something bad happens")
    public void whenSomethingBadHappens(){
        throw new RuntimeException("C'est la vie");
    }

}
