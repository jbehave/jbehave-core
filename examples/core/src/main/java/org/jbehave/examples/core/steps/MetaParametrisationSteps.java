package org.jbehave.examples.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;

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
        assertThat(this.theme, equalTo(theme));
        assertThat(this.variant, equalTo(variant));
    }
}
