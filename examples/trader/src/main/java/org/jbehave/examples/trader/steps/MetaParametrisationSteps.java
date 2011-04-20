package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.junit.Assert;

import static junit.framework.Assert.assertEquals;


public class MetaParametrisationSteps {
    
    private String theme;
    private String variant;

    @Given("I have specified the <theme>")
    public void givenIHaveSpecifiedTheTheme(@Named("theme") String theme) {
        this.theme = theme;
    }

    @Given("a <variant>")
    public void givenAVariant(@Named("variant") String variant){
        this.variant = variant;
    }

    @Given("I have some step, that not node explicity mention meta tags")
    public void givenAThemeAndVariant(@Named("variant") String variant, @Named("theme") String theme) {
        this.theme = theme;
        this.variant = variant;
    }

    @Then("the theme is '%theme' with variant '%variant'")
    public void thenTheThemeAndVariantAre(String theme, String variant) {
        Assert.assertEquals(theme, this.theme);
        Assert.assertEquals(variant, this.variant);        
    }

    // Is ignored by JBehave presently.  Perhaps because of parameter alone.
    // @BeforeScenario
    // public void beforeScenario(@Named("theme") String theme) {
    //     assertEquals("parameters", theme);
    // }


}