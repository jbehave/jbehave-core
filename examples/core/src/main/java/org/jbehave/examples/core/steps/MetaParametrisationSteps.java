package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;

import static org.junit.Assert.assertEquals;

public class MetaParametrisationSteps {

    private String theme;
    private String variant;

    @Given("I have specified the <theme>")
    public void givenIHaveSpecifiedTheTheme(@Named("theme") String theme) {
        this.theme = theme;
    }

    @Given("a <variant>")
    public void givenAVariant(@Named("variant") String variant) {
        this.variant = variant;
    }

    @Given("I have some step that implicitly requires meta params")
    public void givenAThemeAndVariant(@Named("variant") String variant, @Named("theme") String theme) {
        this.theme = theme;
        this.variant = variant;
    }

    @Then("the theme is '$theme' with variant '$variant'")
    public void thenTheThemeAndVariantAre(String theme, String variant) {
        assertEquals(theme, this.theme);
        assertEquals(variant, this.variant);
    }
}
