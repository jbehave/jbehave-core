package org.jbehave.examples.core.meta.steps;

import org.jbehave.core.annotations.Given;
import org.springframework.stereotype.Component;

@Component
public class InputSteps {

    @Given("some input $input")
    public void someInput(final String input) {
        System.out.println(input);
    }

}
