package org.jbehave.examples.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;

public class JsonSteps {

    private JsonDto jsonDto;
    private AnotherJsonDto anotherJsonDto;
    private List<JsonDto> listOfJsonDtos;

    @Given("a json $jsonDto")
    public void givenAJson(final JsonDto jsonDto) {
        this.jsonDto = jsonDto;
    }

    @Given("another json $anotherJson")
    public void givenAnotherJson(final AnotherJsonDto anotherJson) {
        this.anotherJsonDto = anotherJson;
    }

    @Given("a list of jsons $listOfJsons")
    public void givenAListOfJsons(final List<JsonDto> listOfJsons) {
        this.listOfJsonDtos = listOfJsons;
    }

    @Then("the String value is $value")
    public void thenStringValueIs(final String value) {
        assertThat(value, equalTo(this.jsonDto.getString()));
    }

    @Then("the Double value is $value")
    public void thenDoubleValueIs(final Double value) {
        assertThat(value, equalTo(this.anotherJsonDto.getDouble()));
    }

    @Then("the $index{-st|-nd} String value in list is $value")
    public void checkStringValueInList(final int index, final String value) {
        assertThat(value, equalTo(listOfJsonDtos.get(index - 1).getString()));
    }

    @Then("the Integer value is $value")
    public void checkIntegerValue(final Integer value) {
        assertThat(value, equalTo(this.jsonDto.getInteger()));
    }

    @Then("the Boolean value is $value")
    public void checkBooleanValue(final Boolean value) {
        assertThat(value, equalTo(this.anotherJsonDto.getBoolean()));
    }

    @Then("the $index{-st|-nd} Integer value in list is $value")
    public void checkIntegerValueInList(final int index, final Integer value) {
        assertThat(value, equalTo(listOfJsonDtos.get(index - 1).getInteger()));
    }

    @Then("the BigDecimal value is $value")
    public void checkBigDecimalValue(final BigDecimal value) {
        assertThat(value, equalTo(this.jsonDto.getBigDecimal()));
    }

    @Then("the $index{-st|-nd} BigDecimal value in list is $value")
    public void checkBigDecimalValueInList(final int index, final BigDecimal value) {
        assertThat(value, equalTo(listOfJsonDtos.get(index - 1).getBigDecimal()));
    }

    @AsJson
    public static class JsonDto {

        private String string;
        private Integer integer;
        private BigDecimal bigDecimal;

        public String getString() {
            return string;
        }

        public Integer getInteger() {
            return integer;
        }

        public BigDecimal getBigDecimal() {
            return bigDecimal;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @AsJson
    public static class AnotherJsonDto {

        private Double doubleVar;
        private Boolean booleanVar;

        public Double getDouble() {
            return doubleVar;
        }

        public Boolean getBoolean() {
            return booleanVar;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
