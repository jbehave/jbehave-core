package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.model.Verbatim;

public class VerbatimSteps {

    @Given("a verbatim content: $verbatim")
    public void givenVerbatimContent(Verbatim verbatim) {
        System.out.println(verbatim.getContent());
    }

}
