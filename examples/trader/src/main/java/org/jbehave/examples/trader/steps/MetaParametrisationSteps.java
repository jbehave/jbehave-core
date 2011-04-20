package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.junit.Assert;


public class MetaParametrisationSteps {
    
    private String theme;
    private String variant;

    @Given("I have specified the <theme>")
    public void givenIHaveSpecifiedTheTheme(@Named("theme") String theme){
        this.theme = theme;
    }

    @Given("a <variant>")
    public void givenAVariant(@Named("variant") String variant){
        this.variant = variant;
    }

    @Then("the theme is %theme with variant %variant")
    public void thenTheThemeAndVariantAre(String theme, String variant){
        Assert.assertEquals(theme, this.theme);
        Assert.assertEquals(variant, this.variant);        
    }


}