package org.jbehave.examples.threads.steps;

import java.util.concurrent.TimeUnit;

import org.jbehave.core.annotations.When;

public class ThreadsSteps {

    /**
     * TODO  Investigate why if n is expressed as int, random values are passed to method in multi-threading  
     */
    @When("$name counts to $n Mississippi")
    public void whenSomeoneCountsMississippis(String name, String n) {
        System.out.println(name +" starts counting to "+n);
        for (int i = 0; i < Integer.parseInt(n); i++) {
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
