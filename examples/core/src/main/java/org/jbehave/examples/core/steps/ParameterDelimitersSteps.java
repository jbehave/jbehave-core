package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;

public class ParameterDelimitersSteps {

    @Given("a <parameter>")
    public void givenAParameterWithAngleBrackets(@Named("parameter") String parameter) {
        System.out.println(">>>> parameter " + parameter);
    }

    @Given("a [parameter]")
    public void givenAParameterWithSquareBrackets(@Named("parameter") String parameter) {
        System.out.println(">>>> parameter " + parameter);
    }

}
